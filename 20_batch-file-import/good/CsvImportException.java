/**
 * CSV取り込み処理で発生した業務エラーを表す専用の例外。
 *
 * ポイント①：RuntimeException を継承している。
 *   Spring の @Transactional は「RuntimeException が投げられたら自動でロールバック（＝巻き戻し）」する。
 *   つまりこの例外を投げれば、それまでにDBへ入れた分もきれいに取り消される。
 *
 * ポイント②：メッセージに「何行目で・何が」起きたかを込められるようにしておく。
 *   これで呼び出し側（バッチ）は失敗の原因をログや通知にそのまま出せる。
 */
public class CsvImportException extends RuntimeException {

    // 原因（cause）を持たない場合のコンストラクタ（例：列数不正など、こちらで検知したエラー）
    public CsvImportException(String message) {
        super(message);
    }

    // 元の例外（cause）を包んで投げる場合のコンストラクタ（例：ファイル読み込み失敗の IOException）
    // 元の例外を捨てず cause として持たせることで、スタックトレースに根本原因が残る＝握りつぶさない。
    public CsvImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
