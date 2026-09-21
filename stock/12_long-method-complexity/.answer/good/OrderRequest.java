/**
 * 注文の入力内容を運ぶ入れ物（DTO＝画面とサーバの間でデータを運ぶだけのクラス）。
 * ロジックは持たず、値の受け渡しに徹する。
 */
public class OrderRequest {

    private String customerName; // 顧客名
    private String email;        // メールアドレス
    private String phone;        // 電話番号
    private int quantity;        // 注文数量
    private String address;      // お届け先住所
    private String couponCode;   // クーポンコード（任意）

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
