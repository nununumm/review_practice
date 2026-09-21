// 会員を表すクラス。すべてのフィールドが String（素の型）のまま。
public class Member {

    private String email;      // メールアドレス
    private String phone;      // 電話番号
    private String postalCode; // 郵便番号

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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
