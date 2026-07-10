// @Service = 「このクラスは業務ロジック（サービス）担当です」とSpringに登録する目印。
//            これを付けると、他のクラスから「部品」として自動で受け取れるようになる。
@Service
// @RequiredArgsConstructor = Lombokの機能。下の「final付きフィールド」を引数に取る
//            コンストラクタを自動生成してくれる（＝手書きのコンストラクタを省略できる）。
//            これによりSpringが必要な部品を自動で渡してくれる（＝DI：依存性の注入）。
@RequiredArgsConstructor
public class OrderService {

    // ログ出力用の道具（ロガー）を用意。System.out.println の代わりにこれを使うと、
    // 出力レベル(info/warn/error)や出力先をあとから一括制御できる。
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    // 「注文1個につき10ポイント」という固定値。数字を直接コードに埋めず名前を付けておくと、
    // 意味が分かりやすく、変更も1か所で済む（マジックナンバーの回避）。
    private static final int POINT_RATE = 10;

    // final = 一度受け取ったら差し替えないフィールド。Springがここに「部品」を入れてくれる。
    private final OrderRepository orderRepository;   // 注文テーブル操作の担当
    private final StockRepository stockRepository;   // 在庫テーブル操作の担当
    private final PointRepository pointRepository;   // ポイントテーブル操作の担当

    /**
     * 注文を確定する処理。
     * ①在庫を減らす → ②注文を保存 → ③ポイント付与 を「ぜんぶ成功 or ぜんぶ無かったこと」にする。
     */
    // @Transactional = このメソッドの処理をひとまとまり（トランザクション）にする指示。
    //                  途中で例外が起きたら、それまでのDB変更を全部取り消す（ロールバックする）。
    @Transactional
    public OrderResult placeOrder(OrderRequest request) {
        // ④ 入力チェック：注文数が0以下なら、そもそも処理させずエラーにする（業務ルール）。
        if (request.getCount() <= 0) {
            // 不正な入力なので例外を投げる。あとで専用の場所が「400エラー」に変換してくれる。
            throw new IllegalArgumentException("注文数は1以上で指定してください");
        }

        // ① 在庫を減らす。ポイントは「1文のSQLで、在庫が足りるときだけ減らす」こと（詳細はStockRepository）。
        //    戻り値 updated には「更新された行数」が入る（1なら成功、0なら在庫不足）。
        int updated = stockRepository.decreaseStock(request.getProductId(), request.getCount());
        // 更新0件 = 在庫が足りなかった、ということ。例外を投げて処理を止める（→自動でロールバック）。
        if (updated == 0) {
            throw new OutOfStockException(request.getProductId());
        }

        // ② 注文データを新しく作って保存する。
        Order order = new Order(request.getUserId(), request.getProductId(), request.getCount());
        orderRepository.save(order); // DBのINSERT（この時点ではまだトランザクション内の仮状態）

        // ③ ポイント付与：まずユーザーのポイント情報を取ってくる。
        Point point = pointRepository.findByUserId(request.getUserId())
                // findByUserId は「見つからないかもしれない箱(Optional)」を返す。
                // orElseThrow = 中身が空なら例外を投げる、という安全な取り出し方。
                // これで「対象が無いのに処理を続けてNPEで落ちる」事故を防ぐ。
                .orElseThrow(() -> new ResourceNotFoundException("Point not found. userId=" + request.getUserId()));
        // 取得したポイントに、今回付与分（注文数 × 10）を足す。
        point.addAmount(request.getCount() * POINT_RATE);

        // 成功したことをログに記録（あとで「いつ・誰が・何を」注文したか追える）。
        log.info("Order placed. orderId={}, userId={}, productId={}",
                order.getId(), request.getUserId(), request.getProductId());
        // 呼び出し元に返す結果（注文IDだけ入った小さな箱）。
        return new OrderResult(order.getId());
        // ※ ここに try-catch は書かない。例外はわざと外へ投げる。
        //    そうしないと @Transactional のロールバックが働かないため（第3問の最重要ポイント）。
    }
}
