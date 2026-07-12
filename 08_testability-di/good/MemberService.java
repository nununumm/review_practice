package com.example.shop.service;

/**
 * 「今ログインしているユーザーの会員ランクは？」を答えてくれる“窓口”（インターフェース）。
 *
 * ★ポイント★
 * bad版では `MemberUtil.getCurrentRank()` という static 呼び出しだった。
 * static は外から差し替えるのが非常に難しく、テストで「この人はGOLDってことにして」ができない。
 * インターフェースにして“外から渡す”形にすれば、テストでは偽物のランクを返せるようになる。
 */
public interface MemberService {

    // 会員ランク（例: "GOLD" / "NORMAL"）を返す
    String getCurrentRank();
}
