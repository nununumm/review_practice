package com.example.demo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 【受信用DTO】プロフィール更新API（PUT）が "受け取る" 専用の箱。
//
// ★このクラスの最大の役割は「name と email しか持たないこと」。
//   password も role もフィールドが存在しないので、たとえリクエストに
//   "role":"ADMIN" を混ぜて送られても、受け取る口が無い＝無視される。
//   → これがマスアサインメント脆弱性（一括代入の穴）の根本対策。
public class UserUpdateRequest {

    // @NotBlank = null・空文字・空白だけ を禁止する（必須チェック）。
    // @Size     = 文字数の上限を設ける（極端に長い値の保存を防ぐ）。
    @NotBlank(message = "名前は必須です")
    @Size(max = 50, message = "名前は50文字以内で入力してください")
    private String name;

    // @Email = メールアドレスの形式（xxx@xxx）であることをチェックする。
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    // JSON → オブジェクト変換のため、getter/setter を用意する。
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
