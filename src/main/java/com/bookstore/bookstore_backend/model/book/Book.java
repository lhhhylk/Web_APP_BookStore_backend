package com.bookstore.bookstore_backend.model.book;

import com.bookstore.bookstore_backend.model.comment.Comment;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;  // 假设你有Lombok，若无则保留手动getter/setter

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter  // Lombok
@Setter  // Lombok
@Table(name = "books")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @ElementCollection(fetch = FetchType.EAGER)  // 变更：tags改为EAGER（简单集合，性能影响小；或保持LAZY+手动init）
    private List<String> tags = new ArrayList<>();  // 新增：默认空List防null

    @Transient
    private String cover;

    @Column(nullable = false)
    private String price;

    @Transient
    private String description;

    @Column(nullable = false)
    private boolean deleted = false;


    private int sales = 0;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    @Transient
    private int inventory;

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", tags=" + tags +
                ", cover='" + cover + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", deleted=" + deleted +
                ", sales=" + sales +
                ", inventory=" + inventory +
                ", comments=" + comments +
                '}';
    }
}