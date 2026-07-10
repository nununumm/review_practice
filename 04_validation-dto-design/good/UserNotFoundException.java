package com.example.demo.user;

// 「指定されたユーザーが見つからない」ことを表す専用の例外。
// 独自の例外クラスを作っておくと、GlobalExceptionHandler 側で
// 「この例外なら 404 Not Found を返す」と明確に対応づけられる。
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        // エラーメッセージに対象IDを含めておくと、ログでの追跡がしやすい。
        super("ユーザーが見つかりません: id=" + id);
    }
}
