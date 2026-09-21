import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegistrationService {

    private final List<Member> members = new ArrayList<>();

    // 会員を登録する。
    // メール・電話番号・郵便番号をすべて String で受け取る。
    public Member register(String email, String phone, String postalCode) {

        // メール形式チェック
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("メールアドレスの形式が不正です: " + email);
        }
        // 郵便番号チェック（ハイフンを除いた7桁の数字か）
        String digits = postalCode == null ? "" : postalCode.replace("-", "");
        if (digits.length() != 7 || !digits.matches("[0-9]+")) {
            throw new IllegalArgumentException("郵便番号の形式が不正です: " + postalCode);
        }

        Member member = new Member();
        member.setEmail(email);
        member.setPhone(phone);
        member.setPostalCode(postalCode);
        members.add(member);
        return member;
    }

    // 会員のメールアドレスを変更する。
    public void changeEmail(String memberEmail, String newEmail) {
        // メール形式チェック（register と同じ内容をコピペ）
        if (newEmail == null || !newEmail.contains("@") || !newEmail.contains(".")) {
            throw new IllegalArgumentException("メールアドレスの形式が不正です: " + newEmail);
        }
        for (Member m : members) {
            if (m.getEmail().equals(memberEmail)) {
                m.setEmail(newEmail);
                return;
            }
        }
        throw new IllegalArgumentException("会員が見つかりません: " + memberEmail);
    }

    // 会員の配送先郵便番号を変更する。
    public void changePostalCode(String memberEmail, String newPostalCode) {
        // 郵便番号チェック（register と同じ内容をコピペ）
        String digits = newPostalCode == null ? "" : newPostalCode.replace("-", "");
        if (digits.length() != 7 || !digits.matches("[0-9]+")) {
            throw new IllegalArgumentException("郵便番号の形式が不正です: " + newPostalCode);
        }
        for (Member m : members) {
            if (m.getEmail().equals(memberEmail)) {
                m.setPostalCode(newPostalCode);
                return;
            }
        }
        throw new IllegalArgumentException("会員が見つかりません: " + memberEmail);
    }

    // 配送見積もり金額（税込）を計算して返す。
    // 商品金額・配送料をどちらも int の「円」で受け取り、税込の合計を int の「円」で返す。
    public int estimateShipping(int itemPrice, int shippingFee) {
        int subtotal = itemPrice + shippingFee;
        // 消費税10%を掛けて税込にする（1.1倍）
        int total = subtotal * 110 / 100;
        return total;
    }

    // 高額配送（税込5000円以上）かどうかを判定する。
    public boolean isExpensiveShipping(int itemPrice, int shippingFee) {
        int subtotal = itemPrice + shippingFee;
        int total = subtotal * 110 / 100;
        return total >= 5000;
    }
}
