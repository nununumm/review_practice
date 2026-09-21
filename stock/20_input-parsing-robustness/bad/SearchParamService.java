import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SearchParamService {

    /**
     * 画面から来たクエリパラメータ（すべて文字列）を受け取り、
     * 検索条件オブジェクトを組み立てて返す。
     *
     * @param params 例: {"page":"2", "size":"20", "minPrice":"1000", "keyword":"シャツ", "sort":"price"}
     */
    public SearchCondition build(Map<String, String> params) {

        SearchCondition condition = new SearchCondition();

        // ページ番号
        int page = Integer.parseInt(params.get("page"));
        condition.setPage(page);

        // 1ページあたりの件数
        int size = Integer.parseInt(params.get("size"));
        condition.setSize(size);

        // 最低価格での絞り込み
        int minPrice;
        try {
            minPrice = Integer.parseInt(params.get("minPrice"));
        } catch (Exception e) {
            minPrice = 0;
        }
        condition.setMinPrice(minPrice);

        // キーワード
        String keyword = params.get("keyword");
        condition.setKeyword(keyword);

        // 並び順（画面の値をそのまま ORDER BY 句に使う）
        String sort = params.get("sort");
        condition.setOrderByClause("ORDER BY " + sort);

        return condition;
    }

    /**
     * 組み立てた条件から、検索用のSQLを生成する（イメージ）。
     */
    public String toSql(SearchCondition condition) {
        int offset = condition.getPage() * condition.getSize();
        return "SELECT * FROM products"
                + " WHERE price >= " + condition.getMinPrice()
                + " AND name LIKE '%" + condition.getKeyword() + "%'"
                + " " + condition.getOrderByClause()
                + " LIMIT " + condition.getSize()
                + " OFFSET " + offset;
    }

    public static class SearchCondition {
        private int page;
        private int size;
        private int minPrice;
        private String keyword;
        private String orderByClause;

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public int getMinPrice() { return minPrice; }
        public void setMinPrice(int minPrice) { this.minPrice = minPrice; }
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public String getOrderByClause() { return orderByClause; }
        public void setOrderByClause(String orderByClause) { this.orderByClause = orderByClause; }

        // 呼び出し側の便宜用（未使用でも可）
        private static final Map<String, String> EXAMPLE = new HashMap<>();
    }
}
