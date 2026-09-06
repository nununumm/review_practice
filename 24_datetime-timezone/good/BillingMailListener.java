import java.time.format.DateTimeFormatter;

/**
 * 請求が「DBに確定した後」にメールを送る担当。
 *
 * なぜわざわざ分けるのか？
 *   メールは一度送ったら取り消せない（＝ロールバックできない副作用）。
 *   トランザクションの途中で送ってしまうと、その後DB更新が失敗して巻き戻ったとき、
 *   「請求していないのに請求メールだけ届いた」という最悪の状態になる。
 *
 * @TransactionalEventListener(phase = AFTER_COMMIT) は
 * 「トランザクションが無事コミット(＝DBに確定)されたら呼んでね」という予約。
 * ふつうの @EventListener だとコミット前に呼ばれてしまうので、ここでは使わない。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BillingMailListener {

    /** DateTimeFormatter は不変(immutable)＝スレッドセーフなので static で共有してよい */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy年M月d日");

    private final MailSender mailSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionBilled(SubscriptionBilledEvent event) {
        try {
            mailSender.send(
                    event.email(),
                    "ご請求のお知らせ",
                    event.billingDate().format(DATE_FORMAT) + " に " + event.amount() + "円 を請求しました");
        } catch (Exception e) {
            // メール送信の失敗で請求そのものを取り消してはいけない（請求は正しく成立している）。
            // ここではログに残し、必要なら再送キューに積む。黙って消さないことが大事。
            log.error("請求メールの送信に失敗しました subscriptionId={}", event.subscriptionId(), e);
        }
    }
}
