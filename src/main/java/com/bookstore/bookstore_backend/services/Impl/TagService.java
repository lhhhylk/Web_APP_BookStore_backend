package com.bookstore.bookstore_backend.services.Impl;

import com.bookstore.bookstore_backend.model.book.Book;
import com.bookstore.bookstore_backend.model.tag.TagNode;
import com.bookstore.bookstore_backend.repository.BookRepository;
import com.bookstore.bookstore_backend.repository.TagNodeRepository;
import com.bookstore.bookstore_backend.services.ITagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签服务实现
 * 负责构建和管理Neo4j中的标签图
 */
@Service
public class TagService implements ITagService {
    private static final Logger logger = LoggerFactory.getLogger(TagService.class);

    // 顶层标签（根节点）
    private static final Set<String> ROOT_TAGS = Set.of("文学");

    @Autowired
    private TagNodeRepository tagNodeRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public void buildTagGraph() {
        logger.info("开始构建标签图...");
        
        // 从MySQL获取所有图书及其标签（在事务外部执行，避免事务冲突）
        List<Book> allBooks = bookRepository.findAllByDeletedFalse();
        
        // 收集所有标签及其出现频率
        Map<String, Integer> tagFrequency = new HashMap<>();
        // 标签共现关系：Map<标签1, Map<标签2, 共现次数>>
        Map<String, Map<String, Integer>> tagCooccurrenceCount = new HashMap<>();
        
        for (Book book : allBooks) {
            List<String> bookTags = book.getTags();
            if (bookTags == null || bookTags.isEmpty()) {
                continue;
            }
            
            // 统计标签频率
            for (String tag : bookTags) {
                tagFrequency.put(tag, tagFrequency.getOrDefault(tag, 0) + 1);
            }
            
            // 统计标签共现关系（同一本书中的标签，统计共现次数）
            for (int i = 0; i < bookTags.size(); i++) {
                String tag1 = bookTags.get(i);
                tagCooccurrenceCount.putIfAbsent(tag1, new HashMap<>());
                for (int j = i + 1; j < bookTags.size(); j++) {
                    String tag2 = bookTags.get(j);
                    // 增加共现次数
                    tagCooccurrenceCount.get(tag1).put(tag2, 
                        tagCooccurrenceCount.get(tag1).getOrDefault(tag2, 0) + 1);
                    // 双向记录
                    tagCooccurrenceCount.putIfAbsent(tag2, new HashMap<>());
                    tagCooccurrenceCount.get(tag2).put(tag1, 
                        tagCooccurrenceCount.get(tag2).getOrDefault(tag1, 0) + 1);
                }
            }
        }
        
        Set<String> allTags = tagFrequency.keySet();
        logger.info("从MySQL获取到 {} 个唯一标签", allTags.size());
        
        // 在Neo4j事务中执行保存操作
        buildTagGraphInTransaction(allTags, tagCooccurrenceCount);
    }
    
    /**
     * 在Neo4j事务中构建标签图
     */
    @Transactional(transactionManager = "neo4jTransactionManager")
    private void buildTagGraphInTransaction(
            Set<String> allTags,
            Map<String, Map<String, Integer>> tagCooccurrenceCount) {
        
        // 清空现有标签
        clearAllTags();
        
        // 创建所有标签节点
        Map<String, TagNode> tagNodeMap = new HashMap<>();
        for (String tag : allTags) {
            TagNode tagNode = new TagNode(tag);
            tagNodeMap.put(tag, tagNode);
            tagNodeRepository.save(tagNode);
        }
        logger.info("创建了 {} 个标签节点", tagNodeMap.size());
        
        // 分类标签：分类标签（如"中国文学"）和主题标签（如"现实主义"）
        Set<String> categoryTags = identifyCategoryTags(allTags);
        Set<String> themeTags = new HashSet<>(allTags);
        themeTags.removeAll(categoryTags);
        
        logger.info("识别出 {} 个分类标签，{} 个主题标签", categoryTags.size(), themeTags.size());
        
        int relationshipCount = 0;
        
        // 策略1：构建分类标签的层次关系（基于字符串包含关系）
        relationshipCount += buildCategoryHierarchy(tagNodeMap, categoryTags);
        
        // 策略2：为主题标签建立与分类标签的关联（基于共现关系）
        relationshipCount += buildThemeToCategoryRelations(tagNodeMap, themeTags, categoryTags, tagCooccurrenceCount);
        
        // 保存所有节点（包括关系）
        tagNodeRepository.saveAll(tagNodeMap.values());
        
        logger.info("构建了 {} 个标签关系", relationshipCount);
        logger.info("标签图构建完成");
    }
    
    /**
     * 识别分类标签（通常包含"文学"或表示地域/时代的标签）
     */
    private Set<String> identifyCategoryTags(Set<String> allTags) {
        Set<String> categoryTags = new HashSet<>();
        
        // 包含"文学"的标签通常是分类标签
        for (String tag : allTags) {
            if (tag.contains("文学") || 
                tag.contains("小说") || 
                tag.contains("散文") ||
                tag.matches(".*(文学|小说|散文|诗歌|戏剧).*")) {
                categoryTags.add(tag);
            }
        }
        
        // 明确的地域/时代分类
        String[] categoryKeywords = {
            "中国", "外国", "日本", "英国", "美国", "法国", "德国", "俄罗斯",
            "拉丁美洲", "欧洲", "亚洲", "非洲",
            "古代", "古典", "现代", "当代", "近代", "现代", "后现代"
        };
        
        for (String tag : allTags) {
            for (String keyword : categoryKeywords) {
                if (tag.contains(keyword) && tag.length() > keyword.length()) {
                    categoryTags.add(tag);
                    break;
                }
            }
        }
        
        return categoryTags;
    }
    
    /**
     * 构建分类标签的层次关系
     * 规则：
     * 1. 如果标签A是标签B的子串，且B更长，且A不是主题标签，则A是B的父标签
     * 2. 避免创建不必要的关系（如"现实主义"和"魔幻现实主义"不应该有父子关系）
     */
    private int buildCategoryHierarchy(Map<String, TagNode> tagNodeMap, Set<String> categoryTags) {
        int count = 0;
        List<String> sortedCategoryTags = new ArrayList<>(categoryTags);
        sortedCategoryTags.sort((a, b) -> Integer.compare(b.length(), a.length())); // 按长度降序排序
        
        for (int i = 0; i < sortedCategoryTags.size(); i++) {
            String longerTag = sortedCategoryTags.get(i);
            TagNode longerNode = tagNodeMap.get(longerTag);
            
            // 如果已经是顶层标签，跳过
            if (ROOT_TAGS.contains(longerTag)) {
                continue;
            }
            
            // 查找最合适的父标签（最长且是子串的标签）
            String bestParent = null;
            int bestParentLength = 0;
            
            for (int j = i + 1; j < sortedCategoryTags.size(); j++) {
                String shorterTag = sortedCategoryTags.get(j);
                
                // 如果较短的标签是较长标签的子串，且不是完全相同
                if (longerTag.contains(shorterTag) && !longerTag.equals(shorterTag)) {
                    // 检查是否是更合适的父标签（更长的父标签更具体）
                    if (shorterTag.length() > bestParentLength) {
                        // 避免创建错误的关系（如"魔幻现实主义"不应该以"现实主义"为父）
                        // 只有当较短标签是较长标签的前缀或后缀时才建立关系
                        if (longerTag.startsWith(shorterTag) || longerTag.endsWith(shorterTag)) {
                            bestParent = shorterTag;
                            bestParentLength = shorterTag.length();
                        }
                    }
                }
            }
            
            // 如果没有找到合适的父标签，且不是顶层标签，则连接到顶层标签
            if (bestParent == null && !ROOT_TAGS.contains(longerTag)) {
                // 查找顶层标签
                for (String rootTag : ROOT_TAGS) {
                    if (tagNodeMap.containsKey(rootTag) && longerTag.contains(rootTag)) {
                        bestParent = rootTag;
                        break;
                    }
                }
            }
            
            // 建立关系
            if (bestParent != null) {
                TagNode parentNode = tagNodeMap.get(bestParent);
                if (longerNode.getParentTags() == null) {
                    longerNode.setParentTags(new HashSet<>());
                }
                longerNode.getParentTags().add(parentNode);
                count++;
                logger.debug("建立关系: {} -[SUBCATEGORY_OF]-> {}", longerTag, bestParent);
            }
        }
        
        return count;
    }
    
    /**
     * 为主题标签建立与分类标签的关联关系
     * 基于标签共现关系：如果主题标签经常与某个分类标签一起出现，则建立关联
     */
    private int buildThemeToCategoryRelations(
            Map<String, TagNode> tagNodeMap,
            Set<String> themeTags,
            Set<String> categoryTags,
            Map<String, Map<String, Integer>> tagCooccurrenceCount) {
        
        int count = 0;
        int minCooccurrenceThreshold = 1; // 至少共现1次就建立关系
        
        for (String themeTag : themeTags) {
            TagNode themeNode = tagNodeMap.get(themeTag);
            Map<String, Integer> cooccurredTags = tagCooccurrenceCount.getOrDefault(themeTag, new HashMap<>());
            
            // 找出与主题标签共现最多的分类标签（使用实际的共现次数）
            Map<String, Integer> categoryCooccurrenceCount = new HashMap<>();
            
            for (Map.Entry<String, Integer> entry : cooccurredTags.entrySet()) {
                String cooccurredTag = entry.getKey();
                int cooccurrenceTimes = entry.getValue();
                if (categoryTags.contains(cooccurredTag)) {
                    categoryCooccurrenceCount.put(cooccurredTag, cooccurrenceTimes);
                }
            }
            
            // 为主题标签建立与分类标签的关系（最多关联3个最相关的分类标签）
            List<Map.Entry<String, Integer>> sortedCategories = categoryCooccurrenceCount.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .limit(3)
                    .collect(Collectors.toList());
            
            for (Map.Entry<String, Integer> entry : sortedCategories) {
                if (entry.getValue() >= minCooccurrenceThreshold) {
                    String categoryTag = entry.getKey();
                    TagNode categoryNode = tagNodeMap.get(categoryTag);
                    
                    if (themeNode.getParentTags() == null) {
                        themeNode.setParentTags(new HashSet<>());
                    }
                    themeNode.getParentTags().add(categoryNode);
                    count++;
                    logger.debug("建立主题-分类关系: {} -[SUBCATEGORY_OF]-> {} (共现{}次)", 
                        themeTag, categoryTag, entry.getValue());
                }
            }
        }
        
        return count;
    }

    @Override
    public Set<String> findRelatedTags(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return new HashSet<>();
        }
        try {
            List<String> relatedTags = tagNodeRepository.findRelatedTagsWithinTwoHops(tagName);
            Set<String> result = new HashSet<>(relatedTags);
            result.add(tagName); // 确保包含自身
            return result;
        } catch (Exception e) {
            // 当 Neo4j 不可用时，避免整个请求报错，退化为仅使用原始标签
            logger.warn("Neo4j 不可用，使用退化标签搜索（仅使用原始标签: {}）: {}", tagName, e.getMessage());
            return new HashSet<>(Collections.singletonList(tagName));
        }
    }

    @Override
    public Set<String> findRelatedTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        
        // 过滤空标签
        List<String> validTags = tagNames.stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .collect(Collectors.toList());
        
        if (validTags.isEmpty()) {
            return new HashSet<>();
        }
        try {
            List<String> relatedTags = tagNodeRepository.findRelatedTagsWithinTwoHopsForMultiple(validTags);
            Set<String> result = new HashSet<>(relatedTags);
            result.addAll(validTags); // 确保包含输入的标签
            return result;
        } catch (Exception e) {
            // 当 Neo4j 不可用时，避免整个请求报错，退化为仅使用原始标签集合
            logger.warn("Neo4j 不可用，使用退化标签搜索（仅使用原始标签集合: {}）: {}", validTags, e.getMessage());
            return new HashSet<>(validTags);
        }
    }

    @Override
    @Transactional(transactionManager = "neo4jTransactionManager")
    public void clearAllTags() {
        logger.info("清空所有标签节点和关系...");
        tagNodeRepository.deleteAll();
        logger.info("标签图已清空");
    }

    @Override
    @Transactional(transactionManager = "neo4jTransactionManager")
    public void addTagRelationship(String parentTag, String childTag) {
        if (parentTag == null || parentTag.trim().isEmpty()) {
            throw new IllegalArgumentException("父标签不能为空");
        }
        if (childTag == null || childTag.trim().isEmpty()) {
            throw new IllegalArgumentException("子标签不能为空");
        }
        if (parentTag.equals(childTag)) {
            throw new IllegalArgumentException("父标签和子标签不能相同");
        }

        logger.info("建立标签关系: {} 包含 {}", parentTag, childTag);

        // 查找或创建父标签节点
        TagNode parentNode = tagNodeRepository.findByName(parentTag)
                .orElseGet(() -> {
                    TagNode newNode = new TagNode(parentTag);
                    tagNodeRepository.save(newNode);
                    logger.info("创建父标签节点: {}", parentTag);
                    return newNode;
                });

        // 查找或创建子标签节点
        TagNode childNode = tagNodeRepository.findByName(childTag)
                .orElseGet(() -> {
                    TagNode newNode = new TagNode(childTag);
                    tagNodeRepository.save(newNode);
                    logger.info("创建子标签节点: {}", childTag);
                    return newNode;
                });

        // 建立关系：子标签指向父标签（b -[SUBCATEGORY_OF]-> a）
        if (childNode.getParentTags() == null) {
            childNode.setParentTags(new HashSet<>());
        }
        
        // 检查关系是否已存在
        if (!childNode.getParentTags().contains(parentNode)) {
            childNode.getParentTags().add(parentNode);
            tagNodeRepository.save(childNode);
            logger.info("成功建立关系: {} -[SUBCATEGORY_OF]-> {}", childTag, parentTag);
        } else {
            logger.info("关系已存在: {} -[SUBCATEGORY_OF]-> {}", childTag, parentTag);
        }
    }
}

