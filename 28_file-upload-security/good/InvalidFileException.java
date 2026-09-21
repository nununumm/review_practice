/**
 * 「アップロードされたファイルが不正」だと分かったときに投げる専用の例外。
 *
 * ・return null で失敗を握りつぶすのをやめ、「何が起きたか」を型と
 *   メッセージで表現する。呼び出し側は catch して 400 Bad Request 等に変換できる。
 * ・RuntimeException を継承しているので、検査例外のように毎回 throws を
 *   書かなくてよい（Springの例外ハンドラで一括処理する前提）。
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message); // 失敗理由（例:「画像はJPEG/PNGのみ」）を保持する
    }
}
