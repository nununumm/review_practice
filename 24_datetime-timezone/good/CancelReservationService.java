import java.time.Clock;
import java.time.LocalDate;

/**
 * 「解約予定日」の登録を担当するサービス。
 *
 * 元コードの罪は「文字列の変換に失敗しても save() まで進み、
 * 呼び出し側には成功として返っていた」こと。
 * 管理画面には「登録しました」と出るのに、実際は解約日が入っていない。
 * → 障害報告が上がるまで誰も気づけない。
 */
@Service
@RequiredArgsConstructor
public class CancelReservationService {

    /** DateTimeFormatter は不変＝スレッドセーフ。SimpleDateFormat と違い static 共有してよい */
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd");

    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    @Transactional
    public void reserveCancel(Long subscriptionId, String cancelDateText) {
        // 1. 入力そのものの検証（null・空文字を先に弾く）
        if (cancelDateText == null || cancelDateText.isBlank()) {
            throw new IllegalArgumentException("解約予定日を入力してください");
        }

        // 2. 文字列 → 日付への変換。失敗は握りつぶさず、呼び出し側に伝わる例外に変換する。
        //    (この例外は @RestControllerAdvice で受けて 400 Bad Request として返す設計にする)
        LocalDate cancelDate;
        try {
            cancelDate = LocalDate.parse(cancelDateText, INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "解約予定日の形式が正しくありません（yyyy-MM-dd で入力してください）: " + cancelDateText, e);
        }

        // 3. 業務ルールの検証。過去の日付での解約予約は受け付けない。
        LocalDate today = LocalDate.now(clock);
        if (cancelDate.isBefore(today)) {
            throw new IllegalArgumentException("解約予定日には今日以降の日付を指定してください: " + cancelDate);
        }

        // 4. ここまで通って初めて更新する。存在しないIDは .get() ではなく orElseThrow で明示的に落とす。
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));
        sub.setCancelDate(cancelDate);
        subscriptionRepository.save(sub);
    }
}
