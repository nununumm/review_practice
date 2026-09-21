import java.sql.Connection;                          // DBとの接続を表す型
import java.sql.PreparedStatement;                   // SQLを実行する係。値を「?」で安全に埋め込める
import java.sql.ResultSet;                           // 検索結果（表のデータ）を受け取る入れ物
import java.sql.SQLException;                         // JDBCの操作で発生するチェック例外
import javax.sql.DataSource;                          // コネクションプール（接続の貸し出し窓口）を表す型

import org.springframework.beans.factory.annotation.Autowired; // 依存を注入してもらうための印
import org.springframework.stereotype.Repository;    // DBアクセス担当クラスであることを示す印

@Repository
public class UserDao {

    // DriverManager で毎回 new せず、DataSource（＝接続の貸し出し窓口）を「注入」してもらう。
    // これで接続はプール（＝使い回しの池）から借りて返す形になり、接続の枯渇を防げる。
    // テストのときは偽物の DataSource を差し込めるので、テストもしやすくなる。
    private final DataSource dataSource;

    // コンストラクタで受け取る＝DI（依存性の注入）。final なので後から差し替わらず安全。
    @Autowired
    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * idでユーザーを1件検索する。見つからなければ null ではなく独自例外で「居ない」ことを伝える。
     * try-with-resources（＝カッコ内で開いたものを、抜けるとき自動で閉じる書き方）を使う。
     */
    public User findById(long id) {
        // 列は「SELECT *」ではなく必要なぶんだけ名前で明示する。
        // idの値は「?」で渡す＝プレースホルダ。SQLインジェクション（＝文字列連結でSQLを乗っ取られる攻撃）を防ぐ。
        String sql = "SELECT id, name, email FROM users WHERE id = ?";

        // try(...) の中で開いた Connection / PreparedStatement / ResultSet は、
        // 正常でも例外でも、close漏れなく自動で閉じられる（＝リソースリークが起きない）。
        try (Connection conn = dataSource.getConnection();          // プールから接続を借りる
             PreparedStatement ps = prepare(conn, sql, id)) {       // SQLと値をセットした実行係を作る

            try (ResultSet rs = ps.executeQuery()) {                // 検索を実行し、結果も自動で閉じる
                if (rs.next()) {                                    // 1件目があれば
                    return mapRow(rs);                              // 結果を User に詰め替えて返す
                }
                return null;                                        // 見つからないのは正常。ここは null でよい
            }
        } catch (SQLException e) {
            // 原因を握りつぶさず、意味のある独自例外に「翻訳」して呼び出し側へ伝える。
            // 元の例外 e を第2引数に渡すと原因（スタックトレース）が失われない。
            throw new DataAccessException("ユーザー検索に失敗しました。id=" + id, e);
        }
    }

    /**
     * ユーザーの表示名を更新する。更新系も同じく try-with-resources で確実に閉じる。
     */
    public void updateName(long id, String name) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";      // 値は全部「?」で渡す

        try (Connection conn = dataSource.getConnection();          // 接続を借りる
             PreparedStatement ps = conn.prepareStatement(sql)) {   // 実行係を作る（抜けると自動close）

            ps.setString(1, name);                                  // 1番目の「?」に名前を入れる
            ps.setLong(2, id);                                      // 2番目の「?」にidを入れる
            ps.executeUpdate();                                     // 更新を実行

        } catch (SQLException e) {
            // 更新の失敗も黙って捨てず、独自例外で伝える。
            throw new DataAccessException("ユーザー名の更新に失敗しました。id=" + id, e);
        }
    }

    /** SQLと値をセットした PreparedStatement を用意する（呼び出し側で try-with-resources が閉じる） */
    private PreparedStatement prepare(Connection conn, String sql, long id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);          // SQLの実行係を作る
        ps.setLong(1, id);                                          // 「?」にidを安全に埋め込む
        return ps;                                                  // まだ close しない。呼び出し側の try が閉じる
    }

    /** 1行ぶんの結果を User に詰め替える。列は番号ではなく「名前」で取り出す */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));                               // 列名で取る＝SELECTの列順が変わっても壊れない
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        return user;
    }
}
