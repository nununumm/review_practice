/**
 * 「1つの送信手段（メール/SMS/プッシュ）ごとの送り方」を表す共通の窓口（＝インターフェース）。
 * bad では1つの巨大な send メソッドの中に、メールもSMSもプッシュも全部詰め込まれていた。
 * ここでは手段ごとにクラスを分ける（＝Strategy＝やり方を部品として差し替え可能にする設計）。
 * 新しい手段（例: LINE通知）が増えても、この窓口を実装したクラスを1つ足すだけで済む。
 */
public interface ChannelSender {

    // この実装がどの手段を担当するかを返す（サービス側が振り分けに使う）
    NotificationChannel channel();

    // 実際の送信処理。1手段分の責務だけを持つ
    void send(NotificationRequest request);
}
