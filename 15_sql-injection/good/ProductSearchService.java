package com.example.shop.product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;                       // ログ出力用のインターフェース（println の代わり）
import org.slf4j.LoggerFactory;                // Logger を作る工場
import org.springframework.dao.DataAccessException; // Spring がDBエラーを包んで投げる例外
import org.springframework.jdbc.core.JdbcTemplate;  // 安全＆簡潔にSQLを実行できるSpringの道具
import org.springframework.stereotype.Service;

@Service
public class ProductSearchService {

    // このクラス専用のロガー。ログには「どのクラスが出したか」が自動で付く。
    private static final Logger log = LoggerFactory.getLogger(ProductSearchService.class);

    // 【SQLインジェクション対策②】ORDER BY に使ってよい列名の「許可リスト（ホワイトリスト）」。
    // 列名は“値”ではなくSQLの骨組みなのでプレースホルダー(?)が使えない。
    // そこで「あらかじめOKと決めた列名以外は絶対に使わせない」ことで注入を防ぐ。
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "name", "price", "category");
    // ユーザー指定が許可リスト外だったときに使う、安全な既定の並び順。
    private static final String DEFAULT_SORT_COLUMN = "id";

    // JdbcTemplate は内部で PreparedStatement（＝?で値を差し込む仕組み）を使い、
    // 接続やResultSetの後片付け(close)も自動でやってくれる。→ リソースリークも同時に解決。
    private final JdbcTemplate jdbcTemplate;

    // コンストラクタ注入（DI）。Spring が JdbcTemplate を渡してくれる。
    public ProductSearchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 商品名のあいまい検索。カテゴリと並び順も画面から指定できる。
    public List<Product> search(String keyword, String category, String sortColumn) {
        // SQL文を組み立てる箱。値は“直接”書かず、必ず ? （プレースホルダー）にする。
        StringBuilder sql = new StringBuilder("SELECT id, name, price, category FROM products WHERE 1=1");
        // ? に後から差し込む「値」を順番にためておくリスト。
        List<Object> params = new ArrayList<>();

        // 【SQLインジェクション対策①】keyword は連結せず ? にして、値は params 側へ。
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + keyword + "%"); // あいまい検索の % は「値」の側に付けるのが正解
        }
        // category も同様に ? を使う。ユーザー入力を直接SQLに混ぜない。
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category);
        }

        // 【SQLインジェクション対策②】並び順は許可リストで検証してから連結する。
        // 許可リストにあれば採用、なければ既定値(id)に倒す（＝危険な値は無視）。
        String safeSort = ALLOWED_SORT_COLUMNS.contains(sortColumn) ? sortColumn : DEFAULT_SORT_COLUMN;
        sql.append(" ORDER BY ").append(safeSort); // 連結してよいのは“自分が保証した固定値”だけ

        try {
            // query が SQL実行・結果の1行ずつをProductに変換・後片付けまで面倒を見る。
            // params.toArray() が ? に順番どおり差し込まれる（＝バインド変数）。
            return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
        } catch (DataAccessException e) {
            // 【ログ改善】println ではなく logger。例外 e を丸ごと渡すのでスタックトレースが残る。
            // {} は後ろの引数で埋まる。検索条件も一緒に残すと原因調査が速い。
            log.error("商品検索に失敗しました。keyword={}, category={}, sort={}", keyword, category, safeSort, e);
            // 【握りつぶし禁止】空リストで“成功”に見せかけず、独自例外にして呼び出し側へ知らせる。
            throw new ProductSearchException("商品検索に失敗しました", e);
        }
    }

    // ResultSet（DBの検索結果の1行）を Product オブジェクトに詰め替える係。
    // RowMapper という「1行→オブジェクト変換」の型に合わせている。
    private Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getInt("price"));
        p.setCategory(rs.getString("category"));
        return p;
    }
}
