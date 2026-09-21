import java.util.Objects;
import java.util.regex.Pattern;

/**
 * メールアドレスを表す「値オブジェクト」（＝意味のある値を専用の小さな型にしたもの）。
 * ただの String ではなく EmailAddress という型にすることで、
 * 「これはメールアドレスだ」という意味が型そのものに表れる。
 *
 * final を付けて「継承（＝他クラスが機能を上書き）できない」ようにし、
 * フィールドも final にして「一度作ったら中身を変えられない（＝不変・イミュータブル）」にする。
 * 不変だと、他の場所で勝手に書き換えられて壊れる心配がなく、安全に持ち回せる。
 */
public final class EmailAddress {

    // メール形式のチェックに使う正規表現（＝文字の並びのパターン）。
    // 「@ の前後に文字があり、最後に . と文字が続く」ざっくり形式をチェックする。
    private static final Pattern PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    // 実際のメール文字列。final なので生成後は変更できない。
    private final String value;

    /**
     * コンストラクタ（＝オブジェクトを作るための入口）。
     * 「検証（＝正しい値かのチェック）」をここ1箇所に集約する。
     * ここを通らないと EmailAddress は作れないので、
     * 「EmailAddress が存在する＝形式は正しい」ことが保証される。
     */
    public EmailAddress(String value) {
        // null（＝値が無い）や空文字を弾く
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        // 形式が合わなければ例外を投げて、不正な状態のオブジェクトを作らせない
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("メールアドレスの形式が不正です: " + value);
        }
        this.value = value;
    }

    /** 中身の文字列を取り出す（読み取り専用） */
    public String value() {
        return value;
    }

    /**
     * equals（＝2つのオブジェクトが「同じ値か」を判定する）。
     * 値オブジェクトは「中身が同じなら同じもの」とみなすので、value で比較する。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAddress)) return false;
        return value.equals(((EmailAddress) o).value);
    }

    /** hashCode（＝Map や Set で使うための識別番号）。equals と揃える必要がある */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
