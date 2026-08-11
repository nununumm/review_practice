package com.example.cart;

import java.time.Instant;          // 「時点」を表す“不変”の日時型（Date と違い後から書き換えられない）
import java.util.ArrayList;        // 実際にデータを入れられるList実装
import java.util.Collections;      // unmodifiableList（読み取り専用ビュー）を作るためのユーティリティ
import java.util.List;             // リストの型（インターフェイス）

/**
 * ユーザーの買い物カゴを表すクラス。
 *
 * このクラスのねらいは「中身をカート自身が守る」こと。
 * 外部（呼び出し元）が知らないうちにカゴの中身を書き換えられないよう、
 * ① 入口（外のリストと共有しない） ② 出口（原本を渡さない） ③ 変更（意図のある窓口だけ）
 * の3点でカプセル化を徹底している。
 */
public class ShoppingCart {

    // 商品名の一覧。
    // private = このフィールドはクラスの外から直接触れない（バックヤードに隠す）。
    // final  = この「入れ物（リスト）」を別のリストに“差し替え”ない、という宣言。
    private final List<String> items;

    // 作成日時。一度決めたら二度と変わらない値なので final（不変）。
    // Instant は書き換え不可な日時型なので、外に返しても改ざんされない。
    private final Instant createdAt;

    // 最終使用日時。使うたびに更新したい“変わる値”なので final は付けない。
    // ただし更新は markUsed() 経由だけに限定する（全開放の setter は作らない）。
    private Instant lastUsedAt;

    /**
     * カートを新規作成する。
     * 商品リストは外から受け取らず、カート自身が空のリストを内部で用意する。
     * → 外のリストと「同じ1個を共有する」事故（別名／共有参照）を入口から根絶する。
     */
    public ShoppingCart(Instant createdAt) {
        this.items = new ArrayList<>();  // このカート専用の入れ物を新しく生成（外とは無関係）
        this.createdAt = createdAt;      // 作成日時をセット（Instant は不変なのでそのまま持ってよい）
        this.lastUsedAt = createdAt;     // 作った瞬間を「最終使用日時」の初期値にしておく
    }

    /**
     * 商品を1つ追加する。
     * 「追加」という操作の“正式な窓口”をカート自身が持つことで、
     * 外部が内部リストを直接いじる余地をなくす。
     */
    public void addItem(String item) {
        this.items.add(item);            // 内部リストへの追加は、この窓口の中だけで行う
    }

    /**
     * カートの中身を返す（読み取り専用）。
     * unmodifiableList = 「見る・回すのはOK、add/clear しようとすると例外で弾く」ビュー。
     * 内部の原本を“ガラスケース越し”に見せるイメージで、外からの破壊を防ぐ。
     */
    public List<String> getItems() {
        return Collections.unmodifiableList(items);  // 原本(items)ではなく読み取り専用ラッパを返す
    }

    /**
     * 作成日時を返す。
     * Instant は不変なので、そのまま返しても呼び出し元に書き換えられない（防御的コピー不要）。
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 最終使用日時を返す。こちらも Instant なのでそのまま返して安全。
     */
    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    /**
     * 「今、このカートを使った」ことを記録する。
     * 値を外から受け取らず、常に“今の時刻”に更新する。
     * → null・過去・未来のようなおかしな日時を入れられる余地がそもそも無い。
     *   （setLastUsedAt のような「なんでも入る setter」を作らないのがポイント）
     */
    public void markUsed() {
        this.lastUsedAt = Instant.now();  // 更新のルール（=今の時刻）をクラス自身が握る
    }
}
