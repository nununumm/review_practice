package good;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 支払金額の一致判定と割引計算を行うサービス。
 *
 * BigDecimal（＝10進で誤差なく計算できる金額用の型）を使ってはいるが、
 * bad 版には「BigDecimal ならではの罠」が3つ仕込まれていた：
 *   ① double を引数に取るコンストラクタで作ると誤差が入る
 *   ② equals は「値」だけでなく「スケール（小数の桁数）」まで一致しないと true にならない
 *   ③ 割り切れない除算は scale と丸め方を指定しないと例外で落ちる
 * good 版ではこの3つをすべて正しい書き方に直している。
 */
@Service
public class PaymentAmountService {

    // 割引率10%。
    // ★罠①対策：new BigDecimal(0.1) だと double の誤差 0.1000000000000000055... がそのまま入る。
    //   文字列で渡す new BigDecimal("0.1") なら、書いたとおり正確に 0.1 になる。
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.1");

    // 金額を丸めるときの小数桁数（円なので 0 桁＝整数円）を定数化して意味を持たせる
    private static final int MONEY_SCALE = 0;

    /**
     * 顧客の支払額が請求額と一致するか判定する。
     */
    public boolean isPaidInFull(BigDecimal paid, BigDecimal billed) {
        // null は判定できないので、はっきり例外で知らせる
        if (paid == null || billed == null) {
            throw new IllegalArgumentException("支払額・請求額は必須です");
        }
        // ★罠②対策：equals ではなく compareTo(...) == 0 で「値が等しいか」を判定する。
        //   equals はスケール（小数の桁数）まで一致しないと true にならないため、
        //   100（スケール0）と 100.00（スケール2）は数として同じでも equals では false になり、
        //   正しい支払いを「不一致」と誤判定してしまう。
        //   compareTo は「数の大小」だけを見るので、100 と 100.00 を等しいと判定できる。
        return paid.compareTo(billed) == 0;
    }

    /**
     * 請求額に割引を適用し、割引後の支払金額を返す。
     * 割引後 = 請求額 - (請求額 × 割引率)
     */
    public BigDecimal calculateDiscountedAmount(BigDecimal billed) {
        // 負数やnullは計算対象にしない（境界チェック）
        if (billed == null || billed.signum() < 0) {
            throw new IllegalArgumentException("請求額は0以上で指定してください");
        }
        // 請求額 × 割引率 = 値引き額
        BigDecimal discount = billed.multiply(DISCOUNT_RATE);
        // 請求額 - 値引き額 = 割引後の金額。最後に整数円へ四捨五入して端数のブレを無くす
        return billed.subtract(discount).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 請求額を分割回数で割り、1回あたりの支払額を返す。
     */
    public BigDecimal calculateInstallment(BigDecimal billed, int times) {
        if (billed == null || billed.signum() < 0) {
            throw new IllegalArgumentException("請求額は0以上で指定してください");
        }
        // 0回・マイナス回では割れないので弾く（0除算の防止）
        if (times <= 0) {
            throw new IllegalArgumentException("分割回数は1以上で指定してください");
        }
        // ★罠③対策：divide に scale（小数桁数）と RoundingMode（丸め方）を必ず指定する。
        //   これを省くと、1÷3 のような割り切れない計算のとき
        //   「無限小数で表せない」として ArithmeticException で落ちてしまう。
        //   桁数と丸め方を渡せば、指定桁で四捨五入した結果を安全に返せる。
        return billed.divide(new BigDecimal(times), MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
