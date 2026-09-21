package bad;

// 在庫不足を表す業務エラー。
// Exception を継承しているので「チェック例外（＝コンパイラが catch / throws を強制する例外）」。
public class OutOfStockException extends Exception {

    public OutOfStockException(String message) {
        super(message);
    }
}
