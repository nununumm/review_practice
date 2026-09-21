import org.springframework.stereotype.Component;

// プッシュ通知の送信だけを担当する部品（Strategyの実装の1つ）。
@Component
public class PushChannelSender implements ChannelSender {

    private final PushSender pushSender;                     // 実際にプッシュ通知を送る下請け
    private final ReadStatusRepository readStatusRepository; // 既読状態を記録する下請け

    public PushChannelSender(PushSender pushSender, ReadStatusRepository readStatusRepository) {
        this.pushSender = pushSender;
        this.readStatusRepository = readStatusRepository;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH; // この部品はプッシュ通知担当
    }

    @Override
    public void send(NotificationRequest request) {
        User user = request.getUser();
        // 優先度は「緊急かどうか」で決める（文字列 "high"/"normal" は内部だけで組み立てる）
        String priority = request.getMode() == DeliveryMode.URGENT ? "high" : "normal";

        // 静かに送るかどうかで送信方法を分ける
        if (request.getMode() == DeliveryMode.SILENT) {
            pushSender.sendSilent(user.getDeviceToken(), request.getMessage(), priority); // 音なしで送る
        } else {
            pushSender.send(user.getDeviceToken(), request.getMessage(), priority);        // 通常どおり送る
        }

        // 既読管理が必要なら記録する
        if (request.isTrackRead()) {
            readStatusRepository.save(user.getId(), channel().name(), request.getMessage(), false);
        }
    }
}
