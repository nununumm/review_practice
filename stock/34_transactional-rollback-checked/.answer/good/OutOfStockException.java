package good;

// 在庫不足を表す業務エラー。
// この good 版でも「チェック例外（＝コンパイラが catch / throws を強制する例外）」のままにしている。
// なぜなら、今回のメインの直しは「@Transactional(rollbackFor = Exception.class) を明示する」だから。
//
// 【別のやり方】
// もしこのクラスを「extends RuntimeException」に変えれば、
// @Transactional はデフォルト設定のままでもロールバックしてくれる。
// 「呼び出し側に catch / throws を強制したくない」業務エラーなら、
// 実行時例外（RuntimeException 系）にするのも現場でよく使う設計。
// どちらにするかはチームの例外設計の方針として統一しておくとよい。
public class OutOfStockException extends Exception {

    public OutOfStockException(String message) {
        super(message);
    }
}
