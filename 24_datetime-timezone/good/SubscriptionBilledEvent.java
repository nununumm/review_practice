import java.time.LocalDate;

/**
 * 「請求が成立した」という出来事(イベント)を表すオブジェクト。
 *
 * record は「値を持つだけの不変クラス」を1行で作れる文法（Java 16以降）。
 * getter や equals も自動で作られ、あとから中身を書き換えられない＝安全。
 */
public record SubscriptionBilledEvent(
        Long subscriptionId,
        String email,
        long amount,
        LocalDate billingDate) {
}
