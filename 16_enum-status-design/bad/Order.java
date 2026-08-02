package com.example.order;

/**
 * 注文を表すエンティティ。
 * ステータスは文字列で持っている。
 */
public class Order {

    private Long id;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
