/**
 * 入力検証に失敗したことを表す例外（＝処理を中断して「ここがダメだった」と伝える仕組み）。
 *
 * boolean や文字列で「失敗」を表すと、呼び出し側が中身を確認し忘れても
 * コンパイラが気づいてくれず、原因もあいまいになる。
 * 専用の例外にすると「失敗＝例外が飛ぶ」と型で分かり、原因メッセージも一緒に運べる。
 * RuntimeException を継承しているので、呼び出し側は必要な場所だけで catch すればよい。
 */
public class OrderValidationException extends RuntimeException {

    // メッセージ（何がダメだったか）を受け取って親クラスに渡す
    public OrderValidationException(String message) {
        super(message);
    }
}
