import java.util.List;

// APIのレスポンス専用の入れ物（DTO=Data Transfer Object＝データを運ぶためだけの箱）。
// エンティティ（Product）をそのまま返さず、「返したい形」に詰め替えて返すことで、
// APIの見た目とDBの構造を切り離せる（DB都合の変更がAPI仕様に漏れない）。
// <T> は「中身の型は後で決める」という書き方（ジェネリクス）。商品でも注文でも使い回せる。
public class PageResponse<T> {

    private final int page;          // 今のページ番号（0始まり）
    private final int size;          // 1ページあたりの件数
    private final long totalElements; // 全体の総件数（DBのCOUNT結果）
    private final int totalPages;    // 全部で何ページあるか
    private final List<T> content;   // このページの中身

    public PageResponse(int page, int size, long totalElements, int totalPages, List<T> content) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<T> getContent() {
        return content;
    }
}
