package com.example.blog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * ブログ記事の一覧を、著者名を添えて返すサービス。
 *
 * Before（bad）との違い：
 *  1. フィールドインジェクション(@Autowired)をやめ、コンストラクタで依存を受け取る。
 *  2. 「著者名を取る処理をループの中で毎回DBに問い合わせる」のをやめ、
 *     結合(JOIN)済みの結果を1クエリで受け取る（N+1問題の解消）。
 *  3. 全件取得をやめ、ページ単位(Pageable)で取得する。
 */
@Service
public class ArticleService {

    // final を付けることで「生成後に差し替えられない」＝安全。
    private final ArticleRepository articleRepository;

    /**
     * コンストラクタインジェクション。
     *
     * Spring が起動時に、必要な部品(ArticleRepository)をこの引数に渡してくれる。
     * ・@Autowired を書かなくてよい（コンストラクタが1つならSpringが自動で使う）
     * ・テスト時は、テスト用の偽物(モック)を new でそのまま渡せる → テストしやすい
     *   例）new ArticleService(mockRepository)
     * ・依存を final にできる＝不変で堅牢
     */
    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    /**
     * 記事一覧を1ページ分だけ、著者名つきで返す。
     *
     * @param pageable 「何ページ目を・何件・どの順で」欲しいかの指定。
     *                 Controller から渡ってくる想定（例：先頭ページ20件）。
     * @return 記事一覧のDTO（ページ情報つき。総件数や次ページ有無も含む）
     */
    public Page<ArticleDto> getArticleList(Pageable pageable) {
        // たった1回のクエリで、著者名まで詰まったDTOのページが返ってくる。
        // ループも、ループ内のDBアクセスも、もう存在しない。
        return articleRepository.findArticleSummaries(pageable);
    }
}
