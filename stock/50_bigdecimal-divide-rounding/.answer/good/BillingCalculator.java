package good;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 割り勘・税込計算サービス（修正版）。
 *
 * BigDecimal を使うところまでは正しいが、bad 版には BigDecimal 特有の罠が複数あった。
 *
 *  ① divide に丸め方を指定していない：
 *     10 ÷ 3 のように割り切れない割り算は、桁数と丸め方(RoundingMode)を指定しないと
 *     「ArithmeticException: Non-terminating decimal expansion」で落ちる。
 *     割り勘は割り切れないのが普通なので、本番のごく普通の入力で例外になる。
 *
 *  ② new BigDecimal(double) を使っている：
 *     new BigDecimal(0.1) は 0.1 ちょうどではなく 0.1000000000000000055... という
 *     誤差を含んだ値になる（double が 0.1 を正確に表せないため）。せっかく BigDecimal を
 *     使っても、生成の時点で誤差が入ってしまう。文字列 or valueOf から作るのが正解。
 *
 *  ③ 人数のガード（0・負数）が無い：ゼロ除算や負の按分になる。
 */
public class BillingCalculator {

    // 消費税率。マジックナンバーにせず、文字列から生成して誤差を入れない。
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    public BigDecimal perPerson(BigDecimal total, int people) {
        // 人数は 1 以上でなければ割り勘にならない。先に弾く。
        if (people <= 0) {
            throw new IllegalArgumentException("人数は1以上で指定してください: " + people);
        }
        // divide は「小数第何位まで・どう丸めるか」を必ず指定する。
        // 金額なので小数第0位（円単位）・切り上げ（端数は集める側が多めに負担）にしている。
        // 要件次第で HALF_UP（四捨五入）や FLOOR（切り捨て）に変える。
        return total.divide(BigDecimal.valueOf(people), 0, RoundingMode.UP);
    }

    public BigDecimal taxIncluded(BigDecimal price) {
        // 本体価格に (1 + 税率) を掛けて税込を出し、円未満は四捨五入して整数円にする。
        // price は最初から BigDecimal で受け取る（double を経由させて誤差を入れない）。
        return price.multiply(BigDecimal.ONE.add(TAX_RATE))
                .setScale(0, RoundingMode.HALF_UP);
    }
}
