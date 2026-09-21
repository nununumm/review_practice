/**
 * DBアクセスの失敗を表す独自の実行時例外（＝プログラム実行中に投げられる例外）。
 * SQLException のような低レベルな例外を、この「意味のある名前の例外」に翻訳して投げることで、
 * 呼び出し側は「DBで何か失敗した」ことを分かりやすく受け取れる。
 * RuntimeException を継承しているので、呼び出し側に try-catch を強制しない（＝非チェック例外）。
 */
public class DataAccessException extends RuntimeException {

    // メッセージ（何が起きたか）と、原因になった元の例外（cause）の両方を受け取る。
    // cause を渡すことで、元の例外のスタックトレース（＝どこで落ちたかの記録）が失われない。
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
