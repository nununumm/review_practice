import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 決済手数料を計算するサービス。
 *
 * bad 版では「文字列を巨大な if-else で分岐」していたが、good 版では
 * 決済手段ごとのルールを PaymentMethod（enum）側に持たせたので、
 * このクラスは「受け取った手段に計算をお願いする」だけの薄い窓口になっている。
 * 新しい決済手段が増えても、このクラスは一切さわらなくてよい（＝OCP：拡張に開き、修正に閉じる）。
 */
@Service
public class PaymentFeeService {

    /**
     * 決済金額と決済手段を受け取り、手数料額を返す。
     *
     * @param amount 決済金額（誤差の出ない BigDecimal で受け取る）
     * @param method 決済手段（文字列ではなく enum で受け取るので、存在しない値は入り込めない）
     * @return 計算後の手数料額（整数円）
     */
    public BigDecimal calculateFee(BigDecimal amount, PaymentMethod method) {
        // 未知の手段を黙って素通りさせない：null なら、はっきり例外で知らせる
        // （bad 版のように「該当なしで手数料0のまま」だと、バグに気づけず取りこぼす）
        if (method == null) {
            throw new IllegalArgumentException("決済手段が指定されていません");
        }
        // 実際の計算は各手段（PaymentMethod）が自分のルールで行う。ここは委譲するだけ
        return method.calculateFee(amount);
    }

    /**
     * 外部から文字列（例："CREDIT_CARD"）で手段を受け取る入口が必要な場合のヘルパー。
     * 表記ゆれ対策として大文字にそろえてから enum に変換し、
     * 対応する手段が無ければ意味のある例外を投げる（== 比較や握りつぶしをしない）。
     */
    public BigDecimal calculateFee(BigDecimal amount, String methodCode) {
        if (methodCode == null || methodCode.isBlank()) {
            throw new IllegalArgumentException("決済手段が指定されていません");
        }
        try {
            // trim（前後の空白除去）＋ 大文字化で "credit_card" や " CREDIT_CARD " も受け付ける
            PaymentMethod method = PaymentMethod.valueOf(methodCode.trim().toUpperCase());
            return calculateFee(amount, method);
        } catch (IllegalArgumentException e) {
            // valueOf は未知の値だと IllegalArgumentException を投げる。何が問題か分かる形で伝え直す
            throw new IllegalArgumentException("未対応の決済手段です: " + methodCode);
        }
    }
}
