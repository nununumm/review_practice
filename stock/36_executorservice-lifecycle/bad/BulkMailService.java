package bad;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 会員全員へお知らせメールを一斉送信するサービス。
 * 1通ずつ送ると遅いので、スレッドで手分けして並列に送る。
 */
@Service
public class BulkMailService {

    private final MailSender mailSender;

    public BulkMailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 会員リスト全員へメールを送る。
     */
    public void sendToAll(List<Member> members) {
        ExecutorService executor = Executors.newCachedThreadPool();

        for (Member m : members) {
            executor.submit(() -> {
                mailSender.send(m.getEmail(), "お知らせ", "いつもご利用ありがとうございます。");
            });
        }
    }
}
