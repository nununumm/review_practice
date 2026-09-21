package good;

// Spring に「これは部品（コンポーネント）ですよ」と知らせるための注釈（アノテーション）
import org.springframework.stereotype.Component;

/**
 * 会員ランク別の送料計算クラス。
 *
 * 【bad の何が問題だったか】
 *  - 従来型 switch で PLATINUM の case に break を書き忘れていて、
 *    そのまま次の GOLD の処理まで続けて実行されてしまっていた
 *    （＝フォールスルー。break を書かないと次の case へ突き抜ける挙動）。
 *    その結果、送料無料のはずのプラチナ会員が「半額」になっていた。
 *    しかもコンパイル（＝プログラムの文法チェック）は通ってしまうので、
 *    実際に動かすまでバグに気付けなかった。
 *  - default（＝どの case にも当てはまらなかったときの受け皿）が無く、
 *    未知のランクや誤記（"GLOD" など）が来ても何も起きず、
 *    送料が初期値のまま素通りしていた（＝無言のバグ）。
 *
 * 【good の直し方】
 *  - アロー構文の switch 式（＝各分岐が値を直接返す新しい書き方。break が不要で、
 *    構文的にフォールスルーが起こらない）に置き換えた。
 *  - default で例外（＝異常を知らせて処理を止める仕組み）を投げ、
 *    想定外のランクを握りつぶさず即座に気付けるようにした。
 *
 * 【発展】本来ランクは String（文字列）ではなく enum（＝取りうる値を列挙した専用の型）に
 *  すべき。そうすればタイプミスをコンパイル時に弾け、網羅性（＝全ランクを漏れなく扱えているか）
 *  も保証しやすい。ここでは switch の安全化に集中するため String のままにしている。
 */
@Component
public class ShippingFeeCalculator {

    /**
     * 会員ランクに応じて送料を計算する。
     * PLATINUM: 無料 / GOLD: 半額 / SILVER: 基本送料そのまま
     *
     * @param rank    会員ランク（"PLATINUM" / "GOLD" / "SILVER"）
     * @param baseFee 基本送料
     * @return 実際に支払う送料
     */
    public int calculate(String rank, int baseFee) {
        // switch 式：rank の値に応じて「返す値」をそのまま決める。
        // 各分岐は "->" の右側の値を返すだけで、break を書く必要がない
        // （＝隣の case へ突き抜ける事故が構文的に起こらない）。
        return switch (rank) {
            case "PLATINUM" -> 0;             // プラチナ会員は送料無料
            case "GOLD" -> baseFee / 2;       // ゴールド会員は基本送料の半額
            case "SILVER" -> baseFee;         // シルバー会員は基本送料そのまま
            // どのランクにも当てはまらなかったとき（未知のランク・誤記など）は、
            // 黙って変な値を返さず、例外を投げてすぐ気付けるようにする。
            default -> throw new IllegalArgumentException("未知の会員ランク: " + rank);
        };

        // 【参考】どうしても従来型 switch を使う場合は、
        //  すべての case に確実に break を入れ、default も必ず用意すること。
        //  break の付け忘れが一番の事故のもと。
    }
}
