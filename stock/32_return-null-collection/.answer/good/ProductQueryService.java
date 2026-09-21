package good;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 商品照会サービス（改善版）。
 *
 * <p>【bad との違い】
 * bad 版は「見つからないとき null を返す」設計だった。
 *  - 一覧: 0件のとき null を返していた → 呼び出し側がループする前に毎回 null チェックを強いられ、
 *    忘れると NullPointerException（＝null の中身を触ろうとして落ちる例外）の地雷になっていた。
 *  - 単体: 見つからないとき null を返していた → 「無い」ことが型（返り値の見た目）に一切表れず、
 *    呼び出し側が null チェックを忘れて同じく NPE になりやすかった。
 *
 * <p>【good の方針】
 *  - コレクション（＝List などの複数入れ物）は「無い」を <b>空リスト</b> で表す。null は絶対に返さない。
 *    → 呼び出し側は null チェック無しで、そのまま拡張for や stream に流せる（0件なら1回も回らないだけ）。
 *  - 単体は Optional（＝「有るかもしれない／無いかもしれない」を型で表す入れ物）で返す。
 *    → 呼び出し側は orElseThrow / orElse / map など、Optional の作法で「無いとき」を明示的に扱える。
 */
@Service
public class ProductQueryService {

    // DBアクセス担当（＝リポジトリ）。コンストラクタで受け取る（コンストラクタインジェクション）。
    private final ProductRepository repository;

    // Spring が repository を差し込んでくれる（＝DI:依存性の注入）。
    public ProductQueryService(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * カテゴリで商品を一覧取得する。
     * 該当0件でも <b>null ではなく空リスト</b> を返すのがポイント。
     */
    public List<Product> findByCategory(String category) {
        // DBから該当商品を取得する。
        List<Product> products = repository.selectByCategory(category);

        // 取得結果が null（＝リポジトリの実装次第で null が返る可能性）でも安全に空リストへ寄せる。
        // Collections.emptyList() は「要素0個の変更不可なリスト」。毎回同じ実体を使い回すので無駄がない。
        if (products == null) {
            return Collections.emptyList();
        }

        // 0件のときも products（＝空リスト）をそのまま返す。呼び出し側は null チェック不要。
        return products;
    }

    /**
     * 商品コードで1件取得する。
     * 見つからないことを <b>Optional.empty()</b>（＝中身が空のOptional）で表す。
     */
    public Optional<Product> findByCode(String code) {
        // DBから1件取得する（見つからなければ null が返る想定）。
        Product product = repository.selectByCode(code);

        // Optional.ofNullable(...) は「null なら empty、非nullなら中身入りのOptional」を作る便利メソッド。
        // これで「有る／無い」が返り値の型そのものに表れる。
        return Optional.ofNullable(product);

        // 呼び出し側の使い方の例:
        //   service.findByCode("A-001")
        //          .orElseThrow(() -> new ProductNotFoundException(code)); // 無ければ例外
        //   service.findByCode("A-001").map(Product::getName).orElse("(不明)"); // 無ければ既定値
    }
}
