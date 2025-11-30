package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.tag.TagNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Neo4j标签节点Repository
 */
@Repository
public interface TagNodeRepository extends Neo4jRepository<TagNode, String> {
    
    /**
     * 根据标签名称查找标签节点
     */
    Optional<TagNode> findByName(String name);

    /**
     * 查找所有标签节点
     */
    List<TagNode> findAll();

    /**
     * 查找与指定标签通过最多2跳（2次边连接）可以关联到的所有标签
     * 包括：
     * 1. 标签本身
     * 2. 直接连接的标签（1跳）- 包括父标签和子标签
     * 3. 通过2次边连接可以到达的标签（2跳）
     * 注意：Neo4j关系是单向的，所以需要查询两个方向
     */
    @Query("MATCH (start:Tag {name: $tagName}) " +
           "OPTIONAL MATCH path1 = (start)-[:SUBCATEGORY_OF*1..2]->(related1:Tag) " +
           "OPTIONAL MATCH path2 = (start)<-[:SUBCATEGORY_OF*1..2]-(related2:Tag) " +
           "WITH start.name AS startName, collect(DISTINCT related1.name) AS related1Names, collect(DISTINCT related2.name) AS related2Names " +
           "WITH related1Names + related2Names + [startName] AS allTags " +
           "UNWIND allTags AS tagName " +
           "WITH tagName " +
           "WHERE tagName IS NOT NULL " +
           "RETURN DISTINCT tagName")
    List<String> findRelatedTagsWithinTwoHops(@Param("tagName") String tagName);

    /**
     * 查找与指定标签通过最多2跳可以关联到的所有标签（返回TagNode对象）
     */
    @Query("MATCH (start:Tag {name: $tagName}) " +
           "OPTIONAL MATCH path1 = (start)-[:SUBCATEGORY_OF*1..2]->(related1:Tag) " +
           "OPTIONAL MATCH path2 = (start)<-[:SUBCATEGORY_OF*1..2]-(related2:Tag) " +
           "WITH start, related1, related2 " +
           "RETURN DISTINCT COALESCE(related1, related2) AS related")
    Set<TagNode> findRelatedTagNodesWithinTwoHops(@Param("tagName") String tagName);

    /**
     * 查找多个标签通过最多2跳可以关联到的所有标签
     */
    @Query("MATCH (start:Tag) WHERE start.name IN $tagNames " +
           "OPTIONAL MATCH path1 = (start)-[:SUBCATEGORY_OF*1..2]->(related1:Tag) " +
           "OPTIONAL MATCH path2 = (start)<-[:SUBCATEGORY_OF*1..2]-(related2:Tag) " +
           "WITH start.name AS startName, collect(DISTINCT related1.name) AS related1Names, collect(DISTINCT related2.name) AS related2Names " +
           "WITH related1Names + related2Names + [startName] AS allTags " +
           "UNWIND allTags AS tagName " +
           "WITH tagName " +
           "WHERE tagName IS NOT NULL " +
           "WITH collect(DISTINCT tagName) AS allDistinctTags " +
           "UNWIND allDistinctTags AS finalTagName " +
           "RETURN finalTagName")
    List<String> findRelatedTagsWithinTwoHopsForMultiple(@Param("tagNames") List<String> tagNames);
}

