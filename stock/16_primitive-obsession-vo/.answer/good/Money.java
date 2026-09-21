import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金額を表す値オブジェクト（＝意味のある値を専用の型にしたもの）。
 *
 * int の「円」で持ち回ると、税込計算の掛け算で桁あふれ（＝int が扱える範囲を超えて
 * 値が壊れること。オーバーフロー）や、割り算での丸め（＝端数の処理）ミスが起きやすい。
 * そこで、お金の計算に向いた BigDecimal（＝桁数や丸め方を正確に扱える数値型）を内包する。
 */
public final class Money {

    // 金額を BigDecimal で保持する（final なので不変）。
    private final BigDecimal amount;

    // 直接 new させず、意味のある名前のファクトリメソッド（下の yen）経由で作らせる。
    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * 「円」を指定して Money を作る（ファクトリメソッド＝生成専用の名前付き入口）。
     * long で受けることで、int より広い範囲を安全に扱える。
     */
    public static Money yen(long yen) {
        if (yen < 0) {
            throw new IllegalArgumentException("金額は0以上である必要があります: " + yen);
        }
        return new Money(BigDecimal.valueOf(yen));
    }

    /** 足し算。Money 同士でしか足せないので、単位（円）の取り違えが起きない */
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    /**
     * 税率を掛けて税込金額にする。
     * BigDecimal で計算し、端数は切り捨て（RoundingMode.FLOOR）で1円未満を処理する。
     * こうすると「単位」と「丸め方」が型の中に明示され、呼び出し側での掛け算ミスを防げる。
     */
    public Money withTax(BigDecimal taxRate) {
        // amount × (1 + taxRate) を計算し、小数点以下0桁（＝1円単位）に丸める
        BigDecimal multiplier = BigDecimal.ONE.add(taxRate);
        BigDecimal taxed = this.amount.multiply(multiplier).setScale(0, RoundingMode.FLOOR);
        return new Money(taxed);
    }

    /** 大小比較（this が other 以上かどうか）。金額のしきい値判定に使う */
    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    /** 円単位の数値を取り出す */
    public BigDecimal amount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        // BigDecimal は compareTo で比較する（"100" と "100.0" を同じ扱いにするため）
        return this.amount.compareTo(((Money) o).amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toPlainString() + "円";
    }
}
