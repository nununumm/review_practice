package bad;

// クーポン1枚を表すクラス（クーポンコードと割引額を持つ）
public class Coupon {

    private String code;          // クーポンコード（例: "SUMMER2026"）
    private int discountAmount;   // 割引額（円）

    public Coupon(String code, int discountAmount) {
        this.code = code;
        this.discountAmount = discountAmount;
    }

    public String getCode() {
        return code;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDiscountAmount(int discountAmount) {
        this.discountAmount = discountAmount;
    }
}
