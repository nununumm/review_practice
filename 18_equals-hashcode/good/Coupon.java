package good;

import java.util.Objects;

// クーポン1枚を表す「値オブジェクト（value object）」。
// ＝「中身（code と discountAmount）が同じなら、同じクーポンとみなす」オブジェクト。
//
// ポイントは3つ:
//   1. final class にして、継承（＝勝手な作り替え）を防ぐ
//   2. フィールドを final + setter なしにして「不変（immutable）」にする
//   3. equals() / hashCode() を実装して「中身が同じ＝同じ」とJavaに教える
public final class Coupon {

    // final を付けると「一度セットしたら二度と変えられない」フィールドになる
    private final String code;          // クーポンコード（例: "SUMMER2026"）
    private final int discountAmount;   // 割引額（円）

    public Coupon(String code, int discountAmount) {
        // null のクーポンコードは不正なので、ここで弾く（＝おかしな値の混入を早期に防ぐ）
        this.code = Objects.requireNonNull(code, "code は null にできません");
        this.discountAmount = discountAmount;
    }

    // getter だけ用意し、setter は作らない → 生成後は中身が変わらない（不変）
    public String getCode() {
        return code;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    // ── ここが今回の主役 ──
    // 「2つの Coupon が同じか？」をJavaに教えるメソッド。
    // これを書かないと、Javaは「メモリ上の同じ場所にある物か？」でしか比較できない。
    @Override
    public boolean equals(Object o) {
        // 1) まったく同じオブジェクト（同じアドレス）なら、当然 true
        if (this == o) return true;
        // 2) null や、別のクラスの物なら false
        if (o == null || getClass() != o.getClass()) return false;
        // 3) 中身（code と discountAmount）が両方一致すれば「同じクーポン」とみなす
        Coupon other = (Coupon) o;
        return discountAmount == other.discountAmount
                && Objects.equals(code, other.code);
    }

    // equals() を書いたら hashCode() も必ずセットで書く（これがequals/hashCodeの「契約」）。
    // HashSet / HashMap は、まず hashCode() で「だいたいの置き場所」を決めてから
    // equals() で最終確認する。hashCode() がバラバラだと、そもそも equals() まで辿り着けない。
    @Override
    public int hashCode() {
        // equals() で使ったフィールドと「同じ組み合わせ」で計算するのがルール
        return Objects.hash(code, discountAmount);
    }

    // ログやデバッグで中身が読めるようにしておくと親切（必須ではないが実務では推奨）
    @Override
    public String toString() {
        return "Coupon{code='" + code + "', discountAmount=" + discountAmount + "}";
    }
}
