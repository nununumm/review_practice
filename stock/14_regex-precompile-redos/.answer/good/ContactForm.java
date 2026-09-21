public class ContactForm {

    // 問い合わせフォームの1件分のデータ（メール・電話・本文）を持つだけの入れ物クラス。
    private String email;
    private String phone;
    private String message;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
