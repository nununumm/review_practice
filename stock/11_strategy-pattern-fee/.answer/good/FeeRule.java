import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 手数料の計算ルール1式を表す値オブジェクト（＝「率・上限・最低」をまとめた小さな入れ物）。
 *
 * ここに「率をかける → 上限で頭打ち → 最低額で底上げ → 端数を丸める」という計算の流れを
 * 1箇所だけ書いておく。こうすると、各決済手段でこのロジックをコピペせずに済み（＝重複の排除）、
 * ルールを直すときも修正はここ1箇所で完結する。
 */
public class FeeRule {

    // 手数料率（例：0.036 = 3.6%）。BigDecimal（＝誤差なく正確に小数計算できる型）で持つ
    private final BigDecimal rate;

    // 手数料の上限（キャップ）。null なら「上限なし」を意味する
    private final BigDecimal cap;

    // 手数料の最低額（フロア）。null なら「最低額なし」を意味する
    private final BigDecimal floor;

    // 3つのルールを受け取って組み立てる。final なので作った後は変わらない（＝不変で安全）
    public FeeRule(BigDecimal rate, BigDecimal cap, BigDecimal floor) {
        this.rate = rate;
        this.cap = cap;
        this.floor = floor;
    }

    /**
     * 決済金額 amount にこのルールを当てはめて、手数料額を返す。
     * 「率をかける → 上限で抑える → 最低で底上げ → 円未満を四捨五入」という共通の流れ。
     */
    public BigDecimal applyTo(BigDecimal amount) {
        // 1. まず「金額 × 率」で素の手数料を出す
        BigDecimal fee = amount.multiply(rate);

        // 2. 上限があり、それを超えていたら上限に抑える（min＝小さい方を採用）
        if (cap != null) {
            fee = fee.min(cap);
        }

        // 3. 最低額があり、それを下回っていたら最低額まで引き上げる（max＝大きい方を採用）
        if (floor != null) {
            fee = fee.max(floor);
        }

        // 4. 円未満の端数を四捨五入して整数円にする（RoundingMode で丸め方を明示的に指定）
        //    double と違い、BigDecimal は丸め方をこちらで決められるので誤差が出ない
        return fee.setScale(0, RoundingMode.HALF_UP);
    }
}
