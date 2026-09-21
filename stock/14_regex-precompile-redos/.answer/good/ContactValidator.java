import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ContactValidator {

    // 正規表現（＝文字列のパターン照合ルール）は「一度だけ」コンパイルして使い回す。
    // static final にしてクラス読み込み時に1回だけ組み立てるので、
    // 検証のたびに作り直す無駄がなくなる（コンパイル＝ルールを機械が使える形に変換する重い処理）。
    // パターンは「@ の前後にそれぞれ1文字以上」という素直な形。入れ子の (.+)+ のような
    // バックトラッキング（＝後戻り探索）が爆発する書き方を避けて ReDoS を防ぐ。
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    // 電話番号も同じく static final で1回だけコンパイル。
    // 桁数を素直に区切っているだけなので、後戻り探索が暴走することはない。
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^0\\d{1,4}-?\\d{1,4}-?\\d{4}$");

    // 入力文字列の長さ上限（＝ガード）。極端に長い入力を先に弾くことで、
    // 万一パターンが重くても CPU が張り付く被害（ReDoS）を最小化する。
    private static final int MAX_LENGTH = 256;

    /**
     * 問い合わせ一覧から「メールと電話が両方とも有効な件」だけを絞り込んで返す。
     * 該当0件でも null ではなく空リストを返すので、呼び出し側が NPE で落ちない。
     */
    public List<ContactForm> extractValid(List<ContactForm> forms) {
        // filter で「有効な件だけ」を残し、collect で新しいリストにまとめる。
        return forms.stream()
                .filter(this::isValid)
                .collect(Collectors.toList());
    }

    /** 1件が有効かどうかを判定する。メールと電話の両方が形式チェックを通ったときだけ true。 */
    private boolean isValid(ContactForm form) {
        return isValidEmail(form.getEmail()) && isValidPhone(form.getPhone());
    }

    /** メールアドレスの形式チェック。null・長すぎ・パターン不一致は false。 */
    private boolean isValidEmail(String email) {
        // null や長すぎる入力は、正規表現に渡す前にここで弾く（ガード）。
        if (email == null || email.length() > MAX_LENGTH) {
            return false;
        }
        // 使い回している EMAIL_PATTERN で照合するだけ。毎回コンパイルしないので速い。
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /** 電話番号の形式チェック。null・長すぎ・パターン不一致は false。 */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.length() > MAX_LENGTH) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
}
