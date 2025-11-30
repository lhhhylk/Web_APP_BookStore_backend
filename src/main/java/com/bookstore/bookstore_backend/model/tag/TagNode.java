package com.bookstore.bookstore_backend.model.tag;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Neo4j标签节点实体
 * 表示图书标签，标签之间通过关系连接形成分类层次结构
 * 关系类型：SUBCATEGORY_OF 表示子分类关系
 * 例如：外国文学 -[SUBCATEGORY_OF]-> 文学
 */
@Node("Tag")
@Getter
@Setter
public class TagNode {
    @Id
    private String name;  // 标签名称作为唯一标识

    /**
     * 父标签关系（指向更通用的标签）
     * 例如：外国文学 -[SUBCATEGORY_OF]-> 文学
     */
    @Relationship(type = "SUBCATEGORY_OF", direction = Relationship.Direction.OUTGOING)
    private Set<TagNode> parentTags = new HashSet<>();

    public TagNode() {
    }

    public TagNode(String name) {
        this.name = name;
    }
}

