package com.example.auth;

import java.util.UUID;

/**
 * 「再設定トークン（合言葉）を発行する」という役割の窓口（インターフェース）。
 *
 * ★なぜ切り出す？★
 *   bad版では `UUID.randomUUID()` を直接呼んでいた。これは呼ぶたびに毎回ちがう値になる
 *   ＝“非決定的（＝結果が予測できない）”もの。テストで「発行されたトークンが正しく保存され、
 *   メール本文にも入っているか」を確かめたいのに、値が毎回変わると照合しづらい。
 *
 *   そこで「トークンを作る」役割を外出しして、テストでは“いつも同じ値を返す偽物”に
 *   差し替えられるようにする。時刻（Clock）やメール（MailSender）と同じ「外から渡す」発想。
 */
public interface TokenGenerator {

    // 新しいトークン文字列を1つ発行する
    String generate();

    /**
     * 本番用の実装。中身は bad版と同じ UUID だが、
     * 「インターフェース越しに差し替え可能」になった点が大きく違う。
     */
    class UuidTokenGenerator implements TokenGenerator {
        @Override
        public String generate() {
            return UUID.randomUUID().toString();   // ランダムで衝突しにくいトークン
        }
    }
}
