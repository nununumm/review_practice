package com.example.blog.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 入力を受け取る専用の入れ物（DTO＝Data Transfer Object）。
//   Entity(Comment)をそのまま画面入力の受け皿にせず、
//   「外から来る値」に検証ルールを付けて安全に受け止める。
public class CommentForm {

    // @NotBlank … 空文字・空白だけを拒否（未入力を防ぐ）。
    // @Size … 文字数の上限を決める。長すぎる投稿（＝DBやページを荒らす攻撃）を防ぐ。
    // @Pattern … 許可する文字の種類を正規表現で限定する。
    //   ここでは投稿者名を「日本語・英数字・空白・一部記号」だけに絞り、
    //   記号を悪用した攻撃の入り口を狭めている。
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "[\\p{L}\\p{N} 　._-]+")
    private String author;

    @NotBlank
    @Size(max = 1000) // 本文は最大1000文字まで
    private String body;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
