package com.example.blog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 記事テーブルへのアクセスを担当するリポジトリ。
 * JpaRepository を継承すると、findAll などの基本メソッドは自動で使えるようになる。
 */
public interface ArticleRepository extends JpaRepository<Article, Long> {

    /**
     * 記事と著者を「1回のクエリ」で結合（JOIN）して取得し、
     * そのまま ArticleDto に詰めて返す。
     *
     * ここが今回の肝：
     *  - FROM Article a, User u WHERE a.authorId = u.id
     *      → 記事の著者ID と ユーザーの主キーid が一致する行同士をくっつける（＝INNER JOIN 相当）。
     *  - SELECT new com.example.blog.ArticleDto(...)
     *      → 結合結果の各行を、ArticleDto のコンストラクタに渡して直接生成する。
     *
     * これにより、著者名を取るための「ループ内の追加クエリ（N+1問題）」が完全に無くなる。
     * DBアクセスは、このメソッドの呼び出し1回につき「1回」だけ。
     *
     * さらに Pageable を引数に取ることで、
     * 「全件」ではなく「1ページ分（例：20件）」だけを取得する（＝ページネーション）。
     */
    @Query("SELECT new com.example.blog.ArticleDto(a.id, a.title, u.name) "
         + "FROM Article a, User u "
         + "WHERE a.authorId = u.id")
    Page<ArticleDto> findArticleSummaries(Pageable pageable);
}
