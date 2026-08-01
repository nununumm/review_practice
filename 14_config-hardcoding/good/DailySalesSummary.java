package com.example.report.good;

/**
 * 「その日の売上集計結果」を受け取るための入れ物（プロジェクション）。
 *
 * ＜なぜ作るのか？＞
 *  DBに「合計金額」と「注文件数」を計算させて、その2つだけを持ち帰りたい。
 *  Order（注文1件1件）を全部メモリに積むのではなく、集計済みの数字だけ受け取るための型。
 *
 * ＜interface なのに中身が無いのはなぜ？＞
 *  Spring Data JPA が、この getter の名前（getTotalAmount / getOrderCount）に合わせて
 *  中身を自動で用意してくれる（＝自分で実装クラスを書かなくてよい）。
 */
public interface DailySalesSummary {

    // 税抜きの売上合計（DBの SUM(amount) が入る）。1件も無い日は 0 を返すようにSQL側で調整する。
    long getTotalAmount();

    // 注文件数（DBの COUNT(*) が入る）
    long getOrderCount();
}
