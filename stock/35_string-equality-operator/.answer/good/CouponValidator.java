package good;

import org.springframework.stereotype.Component;

/**
 * クーポンコード・会員ランクの判定バリデータ（模範解答）。
 *
 * 【bad の問題】
 *   文字列の一致判定に「==」を使っていた。
 *   「==」は "オブジェクトが同じ場所（＝同じ実体）にあるか" を比べる演算子（＝参照比較）で、
 *   "書いてある文字の中身が同じか" は比べない。
 *   リテラル（"SUMMER2026" のように直接書いた文字列）同士だと、
 *   「文字列プール（＝同じ内容のリテラルを使い回す仕組み）」のおかげで
 *   たまたま同じ実体を指し、開発中は == でも true になってしまう。
 *   ところが本番では、クーポンコードは画面やAPIから送られてくる別インスタンスなので、
 *   中身が "SUMMER2026" でも == は false になり、割引が一切効かない。
 *   → 開発では動いて見えて、本番でだけ壊れる、非常にたちの悪い潜伏バグになる。
 *
 * 【good の直し】
 *   文字列の中身を比べるときは必ず equals を使う。
 *   さらに「定数リテラルを左に置く」書き方（"SUMMER2026".equals(couponCode)）にすると、
 *   couponCode が null でも NullPointerException（＝null に対して操作してしまう例外）にならず、
 *   単に false が返るので安全。
 */
@Component
public class CouponValidator {

    // 割引を判定する。引数はいずれも画面/APIから送られてくる文字列
    public DiscountResult validate(String couponCode, String memberRank) {

        int discountRate = 0; // 合計の割引率を積み上げていく箱

        // クーポンコードの判定：
        // 定数 "SUMMER2026" を左に置いて equals を呼ぶ（couponCode が null でも false になり安全）
        if ("SUMMER2026".equals(couponCode)) {
            discountRate += 20; // 夏クーポンなら +20%
        } else if ("WELCOME".equals(couponCode)) {
            discountRate += 10; // 新規クーポンなら +10%
        }

        // 会員ランクの判定：こちらも equals で「中身」を比べる
        // （ランクのように取りうる値が決まっているものは、本来は enum 化するとさらに安全）
        if ("GOLD".equals(memberRank)) {
            discountRate += 15; // ゴールド会員なら +15%
        } else if ("SILVER".equals(memberRank)) {
            discountRate += 5;  // シルバー会員なら +5%
        }

        // 積み上げた割引率を結果オブジェクトに包んで返す
        return new DiscountResult(discountRate);
    }
}
