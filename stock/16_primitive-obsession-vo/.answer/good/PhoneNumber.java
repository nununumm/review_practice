import java.util.Objects;

/**
 * 電話番号を表す値オブジェクト（＝意味のある値を専用の型にしたもの）。
 * String のままだと「メールアドレスの引数」と取り違えてもコンパイルが通ってしまうが、
 * PhoneNumber という別の型にすれば、取り違えをコンパイル時（＝実行前）に防げる。
 */
public final class PhoneNumber {

    // ハイフンを除いた数字だけで保持する。
    private final String digits;

    /** コンストラクタで検証を集約（10〜11桁の数字を許可する日本の一般的な形式） */
    public PhoneNumber(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("電話番号は必須です");
        }
        // ハイフンを取り除いて数字だけにする
        String normalized = raw.replace("-", "");
        // 10桁または11桁の数字でなければ不正として弾く
        if (!normalized.matches("[0-9]{10,11}")) {
            throw new IllegalArgumentException("電話番号の形式が不正です: " + raw);
        }
        this.digits = normalized;
    }

    /** ハイフン無しの数字を返す */
    public String digits() {
        return digits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber)) return false;
        return digits.equals(((PhoneNumber) o).digits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(digits);
    }

    @Override
    public String toString() {
        return digits;
    }
}
