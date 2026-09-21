import javax.sql.DataSource;                          // コネクションプール（接続の貸し出し窓口）

import org.springframework.dao.EmptyResultDataAccessException; // 検索結果が0件のときに投げられる例外
import org.springframework.jdbc.core.JdbcTemplate;   // 素のJDBCの面倒な後始末を肩代わりしてくれる道具
import org.springframework.stereotype.Repository;

/**
 * 【本命の書き方】JdbcTemplate 版。
 * JdbcTemplate（＝Springが用意した便利ラッパー）は、Connection の開閉・PreparedStatement の close・
 * ResultSet の後始末を「全部自動」でやってくれる。つまり try-with-resources すら自分で書かなくてよい。
 * 実務ではまずこちらを選ぶ。素のJDBC（もう一方の UserDao）は「中で何が起きているか」の学習用。
 */
@Repository
public class UserDaoJdbcTemplate {

    // JdbcTemplate は DataSource から作る。これも「注入」してもらう（自分で new しない）。
    private final JdbcTemplate jdbcTemplate;

    public UserDaoJdbcTemplate(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** idで1件検索。列は名前で指定し、行→Userの詰め替えはラムダ（RowMapper）で書く */
    public User findById(long id) {
        String sql = "SELECT id, name, email FROM users WHERE id = ?"; // 必要な列だけ・値は「?」

        try {
            // queryForObject＝「1件だけ取る」。第2引数のラムダが1行をUserに変換する。
            // rs（結果）から列を「名前」で取り出す＝列順が変わっても壊れない。
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                return user;
            }, id); // 末尾の id が「?」に入る
        } catch (EmptyResultDataAccessException e) {
            return null; // 0件は正常。ここだけ null 許容にする
        }
    }

    /** 表示名を更新。update() が内部で接続の開閉まで面倒を見てくれる */
    public void updateName(long id, String name) {
        String sql = "UPDATE users SET name = ? WHERE id = ?"; // 値は全部「?」
        jdbcTemplate.update(sql, name, id);                    // 順番に「?」へ入る（name→1番目, id→2番目）
    }
}
