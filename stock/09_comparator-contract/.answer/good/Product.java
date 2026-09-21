import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 商品を表すクラス。
 * ここでは「並べ替えのキー（score / salesCount / name）を後から書き換えられない」ように、
 * フィールドをすべて final にして setter を無くした（＝イミュータブル＝作った後は変わらない）。
 * こうしておくと、ソートの途中や後にキーが変わって順序が壊れる、という事故が起きない。
 */
public final class Product {

    // final を付けると「一度だけ代入したら、あとは変更できない」フィールドになる
    private final Long id;
    private final String name;       // null も許容する（表示名が未設定の商品があり得る想定）
    private final int score;         // 人気スコア。int の全範囲（マイナスも巨大値も）を取り得る
    private final long salesCount;   // 売上数。件数が大きくなり得るので long
    private final LocalDateTime updatedAt;

    public Product(Long id, String name, int score, long salesCount, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.salesCount = salesCount;
        this.updatedAt = updatedAt;
    }

    // getter だけ用意する（setter は置かない＝外から書き換えられない）
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public long getSalesCount() {
        return salesCount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * equals は「同じ商品かどうか」を id で判定する。
     * 「並べ替えの順序（compare）」と「等しさ（equals）」は別物なので、
     * compare の結果が 0 でも equals は false になり得る点を、下の compareTo で意識する。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }
        Product other = (Product) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
