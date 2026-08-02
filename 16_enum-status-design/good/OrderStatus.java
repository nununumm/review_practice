package com.example.order;

/**
 * 注文のステータス（状態）を表す「列挙型（enum）」。
 *
 * enum とは？
 *   → 「あらかじめ決めた“決まった値”しか入れられない、専用の型」のこと。
 *   → 文字列 "NEW" のように何でも書ける状態だと、"PAYED" のような打ち間違いや
 *     表記ゆれ（PAID / paid）が入り込む。enum なら“存在しない状態”は
 *     そもそもコンパイルエラーになって書けないので、バグが原理的に消える。
 *
 * ポイント：enum の値（NEW, PAID …）は、プログラム全体で“ただ1つ”しか存在しない。
 *   だから enum どうしの比較は `==` でOK（＝enumでは == が正しい書き方）。
 *   ※文字列の == は「中身」ではなく「メモリ上の場所」を比べてしまうためNGだった。
 */
public enum OrderStatus {

    // 各状態に「画面表示用の日本語ラベル」を持たせておく（後ろの () に渡す値）。
    NEW("ご注文を受け付けました"),
    PAID("お支払いを確認しました"),
    SHIPPED("発送しました"),
    DELIVERED("お届けが完了しました"),
    CANCELLED("キャンセルされました");

    // 各状態が抱える「表示用ラベル」。final ＝ 一度決めたら変えられない。
    private final String label;

    // enum のコンストラクタ。上の NEW(...) などの () の中身がここに渡ってくる。
    // （外から new はできない。enum の値を定義するときだけ内部的に呼ばれる）
    OrderStatus(String label) {
        this.label = label;
    }

    // 画面に出す日本語ラベルを返す。
    // これで getStatusLabel の長い if-else 分岐が不要になる（状態が自分のラベルを知っている）。
    public String getLabel() {
        return label;
    }

    /**
     * この状態からキャンセルできるか？
     * 「まだ発送していない（新規受付 or 支払い済み）」ならキャンセル可能。
     * 判定ロジックを“状態自身”に持たせることで、あちこちに if が散らばるのを防ぐ。
     */
    public boolean isCancellable() {
        return this == NEW || this == PAID;
    }
}
