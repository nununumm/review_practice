package good;

import org.springframework.stereotype.Service;

/**
 * 【このクラスの直しの方針】
 *
 * bad 版は order.getCustomer().getAddress().getZipCode() のように
 * ドット（.）でメソッド呼び出しを数珠つなぎにしていた。
 * これは「デメテルの法則（＝"直接の知り合いとだけ話す"。相手の内部を辿らない、という設計の指針）」違反で、
 * 別名「トレインレック（列車事故＝メソッドが連結された貨車のように数珠つなぎ）」とも呼ばれる。
 *
 * 問題点：
 *  - Order → Customer → Address という「他人の家の中の構造」まで、呼び出し側が丸ごと知ってしまう
 *    （＝結合が強い。将来 Address の持ち方が変わると、この Service まで芋づる式に壊れる）。
 *  - 連鎖の途中が1つでも null だと NullPointerException（NPE）。しかもどのドットで落ちたか分かりにくい。
 *
 * 直し方（Tell, Don't Ask ＝「相手の中身を聞いて自分で処理せず、相手に"やって"と頼む」）：
 *  - 情報を「持っている側」（＝Order）に、必要な項目を返す "窓口メソッド" を持たせる。
 *  - 呼び出し側（この Service）は、直接の知り合いである order とだけ話し、
 *    order.getShippingLabelInfo() のように「最終的に欲しいもの」を頼むだけにする。
 *  - null 安全（＝nullでも安全に動くこと）は、内部構造を知っている Order 側で保証する。
 */
@Service
public class ShippingLabelService {

    // Order を1つ受け取り、配送ラベル文字列を組み立てて返す。
    public String createLabel(Order order) {
        // 呼び出し側は customer や address の存在すら知らなくてよい。
        // 「ラベルに必要な情報を、組み立て済みで渡して」と order に頼むだけ。
        // Order 側が内部（Customer / Address）を辿り、null も吸収して返してくれる。
        return order.getShippingLabelInfo().format();
    }

    /*
     * ------------------------------------------------------------------
     * 【想定する Order 側の "窓口メソッド"（＝この Service が話す相手の設計イメージ）】
     * Order 本体はここでは未定義参照でよいが、次のような形を想定している。
     *
     *   public class Order {
     *       private Customer customer; // 顧客（null の可能性あり）
     *
     *       // ラベルに必要な項目だけを詰めた小さな値オブジェクトを返す窓口。
     *       // 内部の Customer / Address を「辿る」のはこの Order 自身の責務にする
     *       // ＝呼び出し側は構造を知らずに済む（結合が弱くなる）。
     *       public ShippingLabelInfo getShippingLabelInfo() {
     *           // null 安全は "持っている側" のここで面倒を見る。
     *           Customer c = (customer != null) ? customer : Customer.empty();
     *           Address  a = c.getAddress(); // Customer 側も同様に null 安全にしておく
     *           return new ShippingLabelInfo(
     *               a.getZipCode(),     // 郵便番号
     *               a.getPrefecture(),  // 都道府県
     *               c.getName()         // 宛名
     *           );
     *       }
     *   }
     *
     *   // ラベルに載せる項目だけを持つ、小さくて壊れにくい値オブジェクト。
     *   // 「見知らぬ人（Address）」ではなく、この直接の知り合いとだけ Service は話す。
     *   public class ShippingLabelInfo {
     *       private final String zipCode;
     *       private final String prefecture;
     *       private final String name;
     *       // ... コンストラクタ省略 ...
     *
     *       // 文字列への組み立ても "持っている側" に置く（Tell, Don't Ask）。
     *       public String format() {
     *           return "〒" + zipCode + "\n" + prefecture + "\n" + name + " 様";
     *       }
     *   }
     * ------------------------------------------------------------------
     */
}
