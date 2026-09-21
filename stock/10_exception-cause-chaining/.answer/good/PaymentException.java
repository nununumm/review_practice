package com.example.payment;

/**
 * 決済に関するこのアプリ独自の例外の「一番の親」。
 *
 * ポイント（初学者向け）:
 * - RuntimeException を継承した「非チェック例外（＝throws を書かなくてもコンパイルが通る例外）」にしている。
 *   決済の失敗は「呼び出し側が毎回 try-catch を強制されるほど頻繁ではない」ので、
 *   広く伝播させて境界（Controller や @ControllerAdvice）でまとめて扱う設計にする。
 * - この親を継承して「業務例外」「システム例外」に枝分かれさせることで、
 *   呼び出し側が catch する型を選ぶだけで「どういう種類の失敗か」を区別できる。
 */
public abstract class PaymentException extends RuntimeException {

    // message（人が読むメッセージ）と cause（原因になった元の例外）を必ず親に渡す。
    // cause を渡すことで「本当にどこで何が起きたか」のスタックトレースがつながる（＝原因の連鎖）。
    protected PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    // 原因になる例外が無いケース（業務的に断られた等）用のコンストラクタ。
    protected PaymentException(String message) {
        super(message);
    }

    /**
     * この失敗は「もう一度やれば直るかもしれない一時的な失敗」か？を型の利用側に伝えるための目印。
     * true ならリトライ（再試行）する価値がある、false なら何度やっても無駄。
     */
    public abstract boolean isRetryable();
}
