import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 通知送信の司令塔。
 * bad では1つの巨大な send メソッドが全手段・全条件を抱え、
 * さらに引数違いの同名メソッド（オーバーロード）が乱立していた。
 * ここでは「どう送るか」の中身は各 ChannelSender（Strategy）に任せ、
 * このクラスは「リクエストで指定された手段へ振り分けるだけ」に責務を絞る。
 */
@Service
public class NotificationService {

    // 「手段 → その手段を担当する部品」の対応表。振り分けを一発で引けるようにしておく。
    private final Map<NotificationChannel, ChannelSender> sendersByChannel = new EnumMap<>(NotificationChannel.class);

    /**
     * Spring が、ChannelSender を実装したクラス（Mail/Sms/Push）を
     * まとめてリストで注入してくれる（＝DI）。それを手段ごとの対応表に整理する。
     */
    public NotificationService(List<ChannelSender> senders) {
        for (ChannelSender sender : senders) {
            sendersByChannel.put(sender.channel(), sender);
        }
    }

    /**
     * 通知を送る唯一の入口。
     * 引数はフラグの羅列ではなく NotificationRequest 1つだけ。
     * 呼び出し側は下のように、読めば意図が分かる形で書ける：
     *
     *   notificationService.send(
     *       NotificationRequest.builder()
     *           .to(user)
     *           .message("パスワードが変更されました")
     *           .via(NotificationChannel.MAIL, NotificationChannel.PUSH)
     *           .mode(DeliveryMode.URGENT)
     *           .trackRead()
     *           .build());
     */
    public void send(NotificationRequest request) {
        // 指定された手段の分だけ、担当部品に処理を委ねる
        for (NotificationChannel channel : request.getChannels()) {
            ChannelSender sender = sendersByChannel.get(channel);
            if (sender == null) {
                // 対応する部品が無い＝設定漏れ。黙って無視せず、はっきりエラーにする
                throw new IllegalStateException("未対応の通知手段です: " + channel);
            }
            sender.send(request);
        }
    }
}
