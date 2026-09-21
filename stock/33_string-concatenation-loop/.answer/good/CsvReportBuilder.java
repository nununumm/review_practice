package good;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 売上CSVレポートを組み立てるクラス。
 *
 * 【なぜ直したか】
 * bad版はループの中で「csv += ...」と String を継ぎ足していた。
 * String は不変（＝いちど作ったら中身を書き換えられない）なので、
 * "+=" のたびに「今までの全文＋新しい行」をまるごとコピーした新品の文字列を作り、
 * 古い文字列はゴミとして捨てる。これを n 件ぶん繰り返すと、
 * コピー量が 1+2+3+...+n ＝ おおよそ n×n（O(n^2)）に膨らみ、
 * 数万件では急激に遅くなり、捨てた大量のゴミが GC（＝使わなくなったメモリを
 * 自動で片付ける仕組み）を圧迫する。
 *
 * good版は StringBuilder（＝文字列を継ぎ足していく“書き換えできるバッファ”）を使う。
 * 内部の器を使い回して末尾に足すだけなので、全文コピーが起きず処理量は O(n) に収まる。
 */
@Component
public class CsvReportBuilder {

    public String build(List<Order> orders) {
        // StringBuilder ＝ 文字列を継ぎ足していける“書き換え可能な入れ物”。
        // 毎回新しい String を作り直さず、同じ器の末尾に足していくので速い。
        StringBuilder sb = new StringBuilder();

        // 1行目はヘッダ。append で末尾に足す（"+=" と違い全文コピーは起きない）。
        sb.append("ID,顧客,金額\n");

        // 注文の数だけ末尾に1行ずつ足していく。何件になっても全文コピーは発生しない。
        for (Order o : orders) {
            sb.append(o.getId())            // ID
              .append(',')                  // 区切りのカンマ
              .append(o.getCustomerName())  // 顧客名
              .append(',')
              .append(o.getAmount())        // 金額
              .append('\n');                // 改行して次の注文へ
        }

        // 最後に toString() で1本の String に確定させて返す。
        return sb.toString();
    }

    // 【別解】ストリーム（＝一覧を1件ずつ流して加工する書き方）で1行ずつ作り、
    //         Collectors.joining で改行を挟んで1本につなぐ方法もある。
    //         行の作り方が1か所にまとまり、区切り文字の付け忘れも防げる。
    //
    // public String build(List<Order> orders) {
    //     String header = "ID,顧客,金額";
    //     String body = orders.stream()
    //             .map(o -> o.getId() + "," + o.getCustomerName() + "," + o.getAmount())
    //             .collect(java.util.stream.Collectors.joining("\n"));
    //     // ヘッダと本体を改行でつなぐ。String.join も同じ発想の道具。
    //     return String.join("\n", header, body) + "\n";
    // }
}
