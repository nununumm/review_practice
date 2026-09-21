import java.util.Objects;

/**
 * 郵便番号を表す値オブジェクト（＝意味のある値を専用の型にしたもの）。
 * 「7桁の数字」というルールをこの型の中に閉じ込める。
 */
public final class PostalCode {

    // ハイフンを除いた「7桁の数字だけ」の状態で保持する（表記の揺れをなくす）。
    private final String digits;

    /**
     * コンストラクタで検証を集約する。
     * 郵便番号の桁チェックはここ1箇所だけ。呼び出す側はチェック不要になる。
     */
    public PostalCode(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("郵便番号は必須です");
        }
        // ハイフンを取り除いて数字だけにする（"123-4567" も "1234567" も同じ扱いにする）
        String normalized = raw.replace("-", "");
        // 「7桁ちょうどの数字」でなければ不正として弾く
        if (!normalized.matches("[0-9]{7}")) {
            throw new IllegalArgumentException("郵便番号の形式が不正です: " + raw);
        }
        this.digits = normalized;
    }

    /** ハイフン無しの7桁を返す（例: "1234567"） */
    public String digits() {
        return digits;
    }

    /** 表示用にハイフン付きで返す（例: "123-4567"）。書式の知識もこの型に集約する */
    public String formatted() {
        return digits.substring(0, 3) + "-" + digits.substring(3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostalCode)) return false;
        return digits.equals(((PostalCode) o).digits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(digits);
    }

    @Override
    public String toString() {
        return formatted();
    }
}
