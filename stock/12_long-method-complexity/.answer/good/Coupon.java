/**
 * クーポンを表すエンティティ（＝DBの1行に対応するデータのかたまり）。
 */
public class Coupon {

    private String code;     // クーポンコード
    private int remaining;   // 残り利用可能数
    private int usedCount;   // これまでに使われた回数

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }
}
