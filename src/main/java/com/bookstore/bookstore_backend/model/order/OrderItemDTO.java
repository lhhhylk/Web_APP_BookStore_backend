package com.bookstore.bookstore_backend.model.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrderItemDTO {

    @NotNull(message = "书籍ID不能为空")
    private Long bookId;

    @Min(value = 1, message = "数量至少为1")
    private int number = 1;

    private String bookTitle;
    private String bookPrice;
    private String bookCover;
    private BigDecimal lineTotal;

    // Getters and Setters
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getBookPrice() { return bookPrice; }
    public void setBookPrice(String bookPrice) { this.bookPrice = bookPrice; }
    public String getBookCover() { return bookCover; }
    public void setBookCover(String bookCover) { this.bookCover = bookCover; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "OrderItemDTO{" +
                "bookId=" + bookId +
                ", number=" + number +
                ", lineTotal=" + lineTotal +
                '}';
    }
}