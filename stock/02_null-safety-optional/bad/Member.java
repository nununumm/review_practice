package com.example.member;

/**
 * 会員を表すエンティティ。
 * 会員には状態（status）、プレミアム会員フラグ、会員ランク、表示名、メールアドレスがある。
 */
public class Member {

    private Long id;
    private String name;
    private String status;        // "ACTIVE" / "INACTIVE" / "WITHDRAWN" など
    private Boolean premiumFlag;  // プレミアム会員かどうか
    private Integer rank;         // 会員ランク（1〜5）
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPremiumFlag() {
        return premiumFlag;
    }

    public void setPremiumFlag(Boolean premiumFlag) {
        this.premiumFlag = premiumFlag;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
