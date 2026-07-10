package com.example.demo.user;

// 【返信用DTO】API が "外に返す" 専用の箱。
//
// ★このクラスは id / name / email しか持たない。
//   password や role のフィールドが無いので、エンティティに何が入っていようと
//   外へ出るのはこの3項目だけ。→ 情報漏洩（返しすぎ）の根本対策。
//
// また「DBの形（User）」と「APIの形（UserResponse）」を分けておくことで、
// 将来 users テーブルに内部管理用カラムを足しても、APIの応答は勝手に変わらない。
public class UserResponse {

    private final Long id;
    private final String name;
    private final String email;

    // エンティティ（User）から、外に出してよい項目だけを詰め替えるコンストラクタ。
    // 「変換ロジックを1箇所に集約」しておくと、何を公開するかを一元管理できる。
    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        // ↑ password / role は "あえて" コピーしない。これが安全の要。
    }

    // JSON化のための getter（setter は不要＝外から書き換えさせない）。
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
