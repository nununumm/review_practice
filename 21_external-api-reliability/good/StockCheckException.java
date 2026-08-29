package com.example.stock;

/**
 * 在庫確認そのものが実行できなかったことを表す独自例外。
 *
 * bad版は例外を握りつぶして continue していたが、それだと呼び出し元は
 * 「失敗したこと」に気づけない。こうして専用の例外で上へ伝えることで、
 * Controller 側で 503(Service Unavailable) を返すなど適切に対処できる。
 *
 * cause（元の例外）を必ず引き回すことで、「本当の原因」（タイムアウト等）を失わない。
 */
public class StockCheckException extends RuntimeException {

    public StockCheckException(String message, Throwable cause) {
        super(message, cause); // メッセージと原因例外を親クラスに渡して保持する
    }
}
