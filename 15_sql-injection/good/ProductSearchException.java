package com.example.shop.product;

// 商品検索が失敗したことを表す独自例外。
// RuntimeException を継承すると「呼び出し側が必ず try-catch しなくてもよい」非検査例外になる。
// （検索失敗はDB障害など“予期せぬ異常”なので、握りつぶさず上位へ伝播させるのが目的）
public class ProductSearchException extends RuntimeException {

    // message＝人間向けの説明、cause＝本当の原因（元の例外）。
    // cause を渡すことで、元のスタックトレース（どこで壊れたか）が失われない。
    public ProductSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
