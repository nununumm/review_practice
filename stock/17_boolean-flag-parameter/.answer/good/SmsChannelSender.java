import org.springframework.stereotype.Component;

// SMS送信だけを担当する部品（Strategyの実装の1つ）。
@Component
public class SmsChannelSender implements ChannelSender {

    private final SmsSender smsSender;                       // 実際にSMSを送る下請け
    private final ReadStatusRepository readStatusRepository; // 既読状態を記録する下請け

    public SmsChannelSender(SmsSender smsSender, ReadStatusRepository readStatusRepository) {
        this.smsSender = smsSender;
        this.readStatusRepository = readStatusRepository;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS; // この部品はSMS担当
    }

    @Override
    public void send(NotificationRequest request) {
        User user = request.getUser();

        // 送り方（DeliveryMode）で振り分ける。
        // 列挙型なので「URGENTかつSILENT」のような矛盾はそもそも渡ってこない（型で防いである）。
        switch (request.getMode()) {
            case SILENT:
                smsSender.sendWithoutSound(user.getPhoneNumber(), request.getMessage()); // 音を鳴らさず送る
                break;
            case URGENT:
                smsSender.sendUrgent(user.getPhoneNumber(), request.getMessage());        // 緊急で送る
                break;
            case NORMAL:
            default:
                smsSender.send(user.getPhoneNumber(), request.getMessage());              // 通常で送る
                break;
        }

        // 既読管理が必要なら記録する
        if (request.isTrackRead()) {
            readStatusRepository.save(user.getId(), channel().name(), request.getMessage(), false);
        }
    }
}
