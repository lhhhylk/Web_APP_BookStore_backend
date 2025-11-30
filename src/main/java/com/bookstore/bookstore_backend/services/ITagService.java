package com.bookstore.bookstore_backend.services;

import java.util.List;
import java.util.Set;

/**
 * 标签服务接口
 * 负责管理Neo4j中的标签图结构
 */
public interface ITagService {
    
    /**
     * 构建标签图
     * 从MySQL中获取所有标签，分析标签之间的层次关系，并构建到Neo4j中
     */
    void buildTagGraph();

    /**
     * 根据指定的标签，查找通过最多2跳可以关联到的所有标签
     * @param tagName 标签名称
     * @return 关联的标签名称集合（包括自身）
     */
    Set<String> findRelatedTags(String tagName);

    /**
     * 根据多个标签，查找通过最多2跳可以关联到的所有标签
     * @param tagNames 标签名称列表
     * @return 关联的标签名称集合（包括输入的标签）
     */
    Set<String> findRelatedTags(List<String> tagNames);

    /**
     * 清空所有标签节点和关系（用于重新构建）
     */
    void clearAllTags();

    /**
     * 建立标签包含关系：使标签a包含标签b（即b是a的子标签）
     * 在Neo4j中建立关系：b -[SUBCATEGORY_OF]-> a
     * @param parentTag 父标签（标签a）
     * @param childTag 子标签（标签b）
     */
    void addTagRelationship(String parentTag, String childTag);
}

