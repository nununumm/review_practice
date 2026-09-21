package com.example.member;

/**
 * 「指定された ID の会員が見つからなかった」ことを表す独自例外。
 *
 * RuntimeException を継承した「非チェック例外（＝throws を書かなくてよい例外）」にしている。
 * こうすると呼び出し側は毎回 try-catch を強制されず、
 * 「会員がいて当たり前」の処理では素直に書け、いないときだけ上位でまとめて処理できる。
 */
public class MemberNotFoundException extends RuntimeException {

    // メッセージを受け取って親クラス（RuntimeException）に渡すだけのコンストラクタ。
    public MemberNotFoundException(String message) {
        super(message);
    }
}
