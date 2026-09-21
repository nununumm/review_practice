package good;

import java.util.ArrayList;   // 実際にデータを入れておく箱（リストの実装のひとつ）
import java.util.List;        // 「リストという役割」を表す型（中身の実装は問わない）
import java.util.Collections; // 読み取り専用ビューを作る道具などが入っている

/**
 * 会員のポイント履歴クラス。
 *
 * 【何を直したか】
 * bad 版は「リストの機能をそのまま使いたい」という理由で
 *   class PointHistory extends ArrayList<PointEvent>
 * のように ArrayList を継承（＝親クラスの機能を丸ごと受け継ぐこと）していた。
 * しかし「ポイント履歴 は ArrayList である」という関係（is-a＝AはBの一種）は成り立たない。
 * 履歴は「リストを持っている」だけ（has-a＝AはBを持っている）。
 *
 * そこで good 版は、継承をやめて
 * 「List を内部に持ち、必要な仕事だけを外に公開する」委譲（コンポジション）に切り替える。
 *   - 委譲/コンポジション（＝機能を持つ相手を内部に"持って"仕事を任せるやり方）
 *   - カプセル化（＝内部を隠して、決まった窓口だけを外に公開すること）
 *
 * 合言葉：「継承は is-a、has-a なら委譲」。迷ったらまず委譲を選ぶ。
 */
public class PointHistory {

    // ポイントイベントを実際にしまっておく箱。
    // final を付けているので「この箱そのものを別の箱にすげ替える」ことはできない（中身の出し入れはできる）。
    // private なので外から直接いじれない＝勝手に clear() や remove() されない。
    private final List<PointEvent> events = new ArrayList<>();

    // イベントを1件追加する。外から履歴を増やす窓口はこれ「だけ」に絞る。
    public void addEvent(PointEvent e) {
        events.add(e); // 内部のリストに追加する仕事を「任せる」（＝委譲）
    }

    // 今の合計ポイントを計算して返す。
    public int totalPoints() {
        int total = 0;                    // 合計を入れる変数を0で用意
        for (PointEvent e : events) {     // 内部リストのイベントを1件ずつ見て
            total += e.getPoints();       // それぞれのポイントを足し込む
        }
        return total;                     // 足し終えた合計を返す
    }

    // 履歴の中身を「見せる」ための窓口。
    // ただし Collections.unmodifiableList でくるむことで“読み取り専用のビュー”を返す。
    // → 呼び出し側が受け取ったリストに対して add / remove しようとすると例外になり、
    //   外から履歴を勝手に書き換えられない（不変条件＝守りたいルールを守れる）。
    public List<PointEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }
}
