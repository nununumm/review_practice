package com.example.payment;

/**
 * 決済ゲートウェイの呼び出しが「システム的な理由」で失敗したことを表す独自例外。
 *
 * なぜ必要？
 * ・「カードが残高不足で断られた（＝業務的な失敗）」と
 *   「ネットワークが落ちてゲートウェイに繋がらなかった（＝システムの故障）」は、
 *   まったく別モノ。呼び出し側が区別できるように、後者を専用の例外として表現する。
 * ・こうしておくと、上位の層（Controller など）が
 *   「この例外が来たら 502 を返す」「リトライする」といった判断をできる。
 *
 * RuntimeException を継承 = 呼び出し側にtry-catchを強制しない「非検査例外」。
 * 現場では、業務ロジックの例外はこの非検査例外にするのが主流。
 */
public class PaymentGatewayException extends RuntimeException {

    // メッセージ＋「原因となった元の例外(cause)」を受け取るコンストラクタ。
    // cause を渡すことで、元の例外のスタックトレースを失わずに包める（＝例外の連鎖）。
    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
