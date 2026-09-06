import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionBillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final MailSender mailSender;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    /** 毎日0時に起動する日次バッチ。請求日を迎えた契約に請求を立てる */
    @Scheduled(cron = "0 0 0 * * *")
    public void billDueSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        for (Subscription sub : subscriptions) {
            try {
                Date today = new Date();

                // 無料お試し期間(7日)の間は請求しない
                long days = (today.getTime() - sub.getStartDate().getTime()) / (1000 * 60 * 60 * 24);
                if (days < 7) {
                    continue;
                }

                if (sub.getNextBillingDate().before(today)) {
                    Invoice invoice = new Invoice();
                    invoice.setSubscriptionId(sub.getId());
                    invoice.setAmount(sub.getMonthlyFee());
                    invoice.setBilledAt(SDF.format(today));
                    invoiceRepository.save(invoice);

                    // 次回請求日は30日後
                    sub.setNextBillingDate(new Date(today.getTime() + 30L * 24 * 60 * 60 * 1000));
                    subscriptionRepository.save(sub);

                    mailSender.send(sub.getEmail(), "ご請求のお知らせ",
                            SDF.format(today) + " に " + sub.getMonthlyFee() + "円 を請求しました");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** 管理画面から「解約予定日」を登録する（画面から "2026-10-01" のような文字列が来る） */
    public void reserveCancel(Long subscriptionId, String cancelDate) {
        Subscription sub = subscriptionRepository.findById(subscriptionId).get();
        try {
            sub.setCancelDate(SDF.parse(cancelDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        subscriptionRepository.save(sub);
    }
}
