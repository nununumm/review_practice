import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final MailSender mailSender;
    private final SmsSender smsSender;
    private final PushSender pushSender;
    private final ReadStatusRepository readStatusRepository;

    public NotificationService(MailSender mailSender,
                               SmsSender smsSender,
                               PushSender pushSender,
                               ReadStatusRepository readStatusRepository) {
        this.mailSender = mailSender;
        this.smsSender = smsSender;
        this.pushSender = pushSender;
        this.readStatusRepository = readStatusRepository;
    }

    public void send(User user, String message,
                     boolean useMail, boolean useSms, boolean usePush,
                     boolean isUrgent, boolean isSilent, boolean trackRead) {

        if (useMail) {
            String subject = isUrgent ? "【緊急】お知らせ" : "お知らせ";
            if (isUrgent) {
                mailSender.sendHighPriority(user.getEmail(), subject, message);
            } else {
                mailSender.send(user.getEmail(), subject, message);
            }
            if (trackRead) {
                readStatusRepository.save(user.getId(), "MAIL", message, false);
            }
        }

        if (useSms) {
            if (isSilent) {
                smsSender.sendWithoutSound(user.getPhoneNumber(), message);
            } else if (isUrgent) {
                smsSender.sendUrgent(user.getPhoneNumber(), message);
            } else {
                smsSender.send(user.getPhoneNumber(), message);
            }
            if (trackRead) {
                readStatusRepository.save(user.getId(), "SMS", message, false);
            }
        }

        if (usePush) {
            String priority = isUrgent ? "high" : "normal";
            if (isSilent) {
                pushSender.sendSilent(user.getDeviceToken(), message, priority);
            } else {
                pushSender.send(user.getDeviceToken(), message, priority);
            }
            if (trackRead) {
                readStatusRepository.save(user.getId(), "PUSH", message, false);
            }
        }
    }

    public void send(User user, String message, boolean useMail, boolean useSms, boolean usePush) {
        send(user, message, useMail, useSms, usePush, false, false, false);
    }

    public void send(User user, String message, boolean useMail, boolean useSms, boolean usePush, boolean isUrgent) {
        send(user, message, useMail, useSms, usePush, isUrgent, false, false);
    }

    public void sendUrgent(User user, String message, boolean useMail, boolean useSms, boolean usePush) {
        send(user, message, useMail, useSms, usePush, true, false, true);
    }
}
