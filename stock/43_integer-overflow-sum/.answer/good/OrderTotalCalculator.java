package good;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * 注文明細の合計金額を集計するサービス（修正版）。
 *
 * bad 版の問題は「金額を int で扱っていた」こと。int は約 21 億（2,147,483,647）までしか
 * 表せず、それを超えると音もなくマイナスの値に化ける（＝オーバーフロー）。
 *   例）単価 30,000 円 × 数量 100,000 = 30 億 → int では負の数になり、合計が壊れる。
 * しかも例外は出ないので「なぜか合計がマイナス」というバグとして本番で発覚する。
 *
 * good 版では金額を BigDecimal（＝桁あふれや誤差なく、正確に十進計算できる型）で扱い、
 * 割り算にはゼロ除算チェックと丸め方法(RoundingMode)を明示する。
 */
public class OrderTotalCalculator {

    public BigDecimal calculateTotal(List<OrderLine> lines) {
        // null を渡されても落ちないよう、はっきり弾く（防御的なガード）
        Objects.requireNonNull(lines, "lines は null にできません");

        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            // 単価 × 数量。BigDecimal 同士なので桁あふれの心配がない。
            BigDecimal lineAmount = line.getUnitPrice()
                    .multiply(BigDecimal.valueOf(line.getQuantity()));
            total = total.add(lineAmount);
        }
        return total;
    }

    public BigDecimal averagePerItem(List<OrderLine> lines) {
        BigDecimal total = calculateTotal(lines);

        long count = 0;
        for (OrderLine line : lines) {
            count += line.getQuantity();
        }

        // 総数量が 0 のときに割り算するとゼロ除算で落ちる。先に弾いて意味のある例外にする。
        if (count == 0) {
            throw new IllegalArgumentException("明細の総数量が 0 のため平均単価を計算できません");
        }

        // BigDecimal の割り算は「小数第何位まで／どう丸めるか」を必ず指定する。
        // ここでは金額なので小数第0位・四捨五入（HALF_UP）にしている。
        return total.divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP);
    }
}
