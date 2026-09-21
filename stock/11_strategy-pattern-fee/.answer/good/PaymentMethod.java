import java.math.BigDecimal;

/**
 * 決済手段（＝支払いのやり方）を表す列挙型（enum）。
 *
 * ポイントは「決済手段ごとの手数料ルールを、その手段自身に持たせている」こと。
 * こうすると新しい手段が増えても、ここに1行 enum を足して数字を書くだけで済み、
 * 計算サービス側（PaymentFeeService）の if-else をいじる必要がなくなる（＝OCP：拡張に開き、修正に閉じる）。
 *
 * 各手段は「手数料率・上限・最低手数料」をまとめた FeeRule（＝手数料ルール1式）を1つ持つ。
 * 実際の計算そのものは FeeRule に集約してあるので、ここは「どの手段がどんな数字か」の一覧表に徹する。
 */
public enum PaymentMethod {

    // クレジットカード：料率3.6%、上限5000円、最低50円
    CREDIT_CARD(new FeeRule(new BigDecimal("0.036"), new BigDecimal("5000"), new BigDecimal("50"))),

    // コンビニ払い：料率2.5%、上限5000円、最低60円
    CONVENIENCE(new FeeRule(new BigDecimal("0.025"), new BigDecimal("5000"), new BigDecimal("60"))),

    // 銀行振込：料率1.0%、上限5000円、最低なし（null＝下限を設けない）
    BANK_TRANSFER(new FeeRule(new BigDecimal("0.01"), new BigDecimal("5000"), null)),

    // QRコード決済：料率1.8%、上限なし（null）、最低30円
    QR_CODE(new FeeRule(new BigDecimal("0.018"), null, new BigDecimal("30")));

    // この手段の手数料ルール。final にして、後から書き換えられないようにする（＝不変で安全）
    private final FeeRule feeRule;

    // enum の各要素を作るときに、その手段のルールを受け取って持っておくコンストラクタ
    PaymentMethod(FeeRule feeRule) {
        this.feeRule = feeRule;
    }

    /**
     * この決済手段のルールにしたがって手数料を計算する。
     * 計算式は FeeRule 側に1本化してあるので、ここはただ委譲（＝お任せ）するだけ。
     */
    public BigDecimal calculateFee(BigDecimal amount) {
        return feeRule.applyTo(amount);
    }
}
