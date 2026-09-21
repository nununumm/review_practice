package bad;

import java.util.ArrayList;

// 会員のポイント履歴。
// add() や size() など、リストの機能をそのまま使いたかったので
// ArrayList を継承して作った。
public class PointHistory extends ArrayList<PointEvent> {

    // イベントを1件追加する。
    public void addEvent(PointEvent e) {
        add(e);
    }

    // 全イベントのポイントを合計して、今の合計ポイントを返す。
    public int totalPoints() {
        int total = 0;
        for (PointEvent e : this) {
            total += e.getPoints();
        }
        return total;
    }
}
