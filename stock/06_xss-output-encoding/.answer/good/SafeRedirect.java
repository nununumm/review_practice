package com.example.blog.comment;

// 「どうしても遷移先を可変にしたい」場合の安全なreturnUrlの扱い方を示す補助クラス。
//   基本方針（今回のController）は "returnUrlを使わずサーバー側でURLを組む" こと。
//   だが要件上どうしても受け取る必要があるなら、次のように厳しく絞る。
public final class SafeRedirect {

    private SafeRedirect() { // インスタンス化しないユーティリティ
    }

    // 受け取ったreturnUrlが「自サイト内の相対パス」だけかを検証する。
    //   安全でなければ既定ページ（例：記事一覧）へ倒す（＝フェイルセーフ）。
    public static String resolve(String returnUrl) {
        if (returnUrl == null || returnUrl.isBlank()) {
            return "/articles"; // 何も無ければ安全な既定ページへ
        }
        // "/" で始まる相対パスだけを許可する。
        //   ただし "//evil.com"（プロトコル相対URL）は外部サイト扱いなので弾く。
        //   "/\evil.com" のようなバックスラッシュ混入も外部誘導に使われるため弾く。
        boolean isSiteRelative = returnUrl.startsWith("/")
                && !returnUrl.startsWith("//")
                && !returnUrl.startsWith("/\\");
        if (!isSiteRelative) {
            return "/articles"; // http:// や //host など外部URLは拒否
        }
        return returnUrl;
    }
}
