import org.springframework.stereotype.Component;

// メール送信だけを担当する部品（Strategyの実装の1つ）。
// @Component を付けると Spring がこのクラスを自動で作って管理してくれる（＝DIの対象になる）。
@Component
public class MailChannelSender implements ChannelSender {

    private final MailSender mailSender;                     // 実際にメールを送る下請け
    private final ReadStatusRepository readStatusRepository; // 既読状態を記録する下請け

    public MailChannelSender(MailSender mailSender, ReadStatusRepository readStatusRepository) {
        this.mailSender = mailSender;
        this.readStatusRepository = readStatusRepository;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.MAIL; // この部品はメール担当
    }

    @Override
    public void send(NotificationRequest request) {
        User user = request.getUser();
        // 緊急かどうかを boolean ではなく DeliveryMode（列挙型）で判定する
        boolean urgent = request.getMode() == DeliveryMode.URGENT;
        // 件名は緊急かどうかで切り替える
        String subject = urgent ? "【緊急】お知らせ" : "お知らせ";

        // 緊急なら高優先度で、そうでなければ通常で送る
        if (urgent) {
            mailSender.sendHighPriority(user.getEmail(), subject, request.getMessage());
        } else {
            mailSender.send(user.getEmail(), subject, request.getMessage());
        }

        // 既読管理が必要なら記録する（この判断も各手段の中に閉じ込める）
        if (request.isTrackRead()) {
            readStatusRepository.save(user.getId(), channel().name(), request.getMessage(), false);
        }
    }
}
