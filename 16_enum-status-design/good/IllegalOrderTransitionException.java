package com.example.order;

/**
 * 「その状態からは、その操作に進めない」ことを表す独自例外。
 *
 * なぜ println ではなく“例外”にするのか？
 *   → bad/ では、進めないときに System.out.println で表示して“素通り”していた。
 *     これだと呼び出し元は「成功したのか失敗したのか」を判断できず、
 *     支払い前の注文を発送してしまう…といった事故に気づけない。
 *   → 「進めない」は“正常ではない事態”なので、例外で明確に知らせて処理を止めるのが正解。
 *
 * RuntimeException を継承 ＝ 呼び出し側に try-catch を強制しない「非検査例外」。
 *   状態遷移の誤りは通常“プログラム側のミス”なので、
 *   個別に握りつぶさず、上位（例：Spring の例外ハンドラ）でまとめて扱う想定。
 */
public class IllegalOrderTransitionException extends RuntimeException {

    public IllegalOrderTransitionException(String message) {
        super(message);
    }
}
