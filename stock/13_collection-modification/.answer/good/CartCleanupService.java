import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 買い物カゴ整理サービス。
 *
 * ポイント：@Service（＝Springが1つだけ作って使い回す部品）は、
 * 全リクエストで共有されるので「自分専用のデータ」を持たせてはいけない。
 * ここではカートの中身をフィールドに持たず、メソッドの引数として受け取り、
 * 戻り値として返す「状態を持たない（ステートレスな）」作りにしている。
 */
@Service
public class CartCleanupService {

    /**
     * カートから在庫切れ・販売終了の商品を取り除き、残った商品を返す。
     *
     * @param cart 呼び出し側が持っているカートの中身（この中身自体は書き換えない）
     * @return 整理後の商品リスト（変更不可。呼び出し側から add/remove できない）
     */
    public List<CartItem> cleanupUnavailable(List<CartItem> cart) {
        // 引数がnull（＝中身が渡されていない）なら、空リストとして扱って落ちないようにする
        if (cart == null) {
            return List.of(); // List.of() は「空で変更もできないリスト」
        }

        // 渡されたリストを直接いじると呼び出し側のデータまで変わってしまうので、
        // まず自分用のコピー（＝複製）を作ってから作業する（＝防御的コピー）
        List<CartItem> working = new ArrayList<>(cart);

        // removeIf：条件に合う要素をまとめて安全に削除する。
        // 拡張for文の途中で remove すると ConcurrentModificationException
        // （＝反復中に中身を書き換えて落ちる例外）になるが、removeIf は内部で
        // 正しく削除してくれるので、その心配がない。
        working.removeIf(item ->
                item == null                 // null要素はここで弾く（後段のNPE防止）
                        || item.isSoldOut()  // 販売終了
                        || item.getStock() <= 0); // 在庫切れ

        // 返すときは List.copyOf で「変更できないリスト」にして渡す。
        // こうすれば呼び出し側が勝手に add/remove して内部状態を壊すことができない。
        return List.copyOf(working);
    }

    /**
     * 整理後のカートに残った商品の合計点数（数量の合計）を数える。
     * null要素は cleanupUnavailable の時点で除いてあるが、念のためここでも弾く。
     */
    public int totalQuantity(List<CartItem> cart) {
        if (cart == null) {
            return 0;
        }
        int total = 0;
        for (CartItem item : cart) {
            // nullを足そうとすると getQuantity() で NullPointerException になるので防ぐ
            if (item != null) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    /**
     * カートに商品を1件追加した新しいリストを返す（元のリストは書き換えない）。
     * null を追加しようとしたら、その場ではっきり例外にして早めに気づけるようにする。
     */
    public List<CartItem> addItem(List<CartItem> cart, CartItem item) {
        // Objects.requireNonNull：nullならすぐ分かりやすい例外を投げる（後で謎のNPEになるのを防ぐ）
        Objects.requireNonNull(item, "追加する商品がnullです");

        List<CartItem> working = (cart == null) ? new ArrayList<>() : new ArrayList<>(cart);
        working.add(item);
        return List.copyOf(working); // 変更不可リストで返す
    }
}
