import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductListService {

    // 1ページで返せる最大件数の上限（＝クランプ値）。
    // これを超える size が来ても、この値まで“切り詰める”ことで
    // 「size=1000000で全件ロード → メモリ枯渇」を防ぐ。
    private static final int MAX_SIZE = 100;

    // size が指定されなかった/おかしいときに使うデフォルト件数。
    private static final int DEFAULT_SIZE = 20;

    private final ProductRepository productRepository;

    // コンストラクタで依存を受け取る（コンストラクタインジェクション）。
    // フィールドを final にでき、テスト時にモックを差し込みやすい。
    public ProductListService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 指定ページの商品を「そのページ分だけ」DBから取得して返す。
    // 戻り値の Page には、中身のリストだけでなく総件数・総ページ数も入っている。
    public Page<Product> getProducts(int page, int size) {

        // ① 入力を安全な範囲に整える（下限チェック＋上限クランプ）
        //    page が負なら 0 に、size が 1 未満ならデフォルトに、上限超えなら MAX_SIZE に。
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        // ② Pageable = 「何ページ目を、何件ずつ、どう並べて欲しいか」をまとめた注文票。
        //    Spring Data がこの注文票から自動で LIMIT / OFFSET を組み立ててくれるので、
        //    offset を自分で int 掛け算する必要がなく、オーバーフローの心配もない。
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("id"));

        // ③ DBに委譲。findAll(Pageable) は “そのページ分の行” と “総件数(COUNT)” を
        //    まとめて取ってくる。全件をメモリに載せない。
        return productRepository.findAll(pageable);

        // 【発展】ページ番号がとても大きい（OFFSET が巨大）と、DBが「読み飛ばし」に
        // 時間を使い遅くなる。無限スクロール等で深いページに潜るなら、
        // 「前回の最後のidより後ろを N件」と条件で辿る keyset(seek)ページング
        //   例: WHERE id > :lastId ORDER BY id LIMIT :size
        // にするとOFFSETが要らず一定速度になる。
    }
}
