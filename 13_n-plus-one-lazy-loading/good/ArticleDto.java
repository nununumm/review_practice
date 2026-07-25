package com.example.blog;

/**
 * 画面に「記事一覧」を表示するための、表示専用のデータ入れ物（DTO）。
 *
 * ポイント：Entity（＝DBのテーブルとそのまま対応する重いオブジェクト）を
 * そのまま画面に返さず、「画面に必要な項目だけ」を持つ軽い箱を用意している。
 * （Entity≠DTO、DTOは用途ごとに作る、という第4問で学んだ原則の実践）
 */
public class ArticleDto {

    // 記事ID（画面でのリンクや詳細遷移に使う想定）
    private final Long id;
    // 記事タイトル
    private final String title;
    // 著者の「名前」（IDではなく、画面に出す表示名）
    private final String authorName;

    /**
     * すべての項目を受け取るコンストラクタ。
     *
     * ★重要★ このコンストラクタは、後述の Repository の JPQL から
     * 「SELECT new ...ArticleDto(a.id, a.title, u.name)」という形で
     * 直接呼び出される。DBから取った1行が、そのままこの箱に詰められる。
     * （＝「コンストラクタ・プロジェクション」という手法）
     */
    public ArticleDto(Long id, String title, String authorName) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
    }

    // --- 以下は値を読み出すためのゲッター（setterは作らない＝作った後に書き換えられない不変オブジェクト） ---

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorName() {
        return authorName;
    }
}
