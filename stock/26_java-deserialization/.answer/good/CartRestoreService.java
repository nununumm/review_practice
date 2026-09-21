package good;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * カート状態を復元するサービス。
 *
 * bad 版では、外部（ブラウザ）から来た信頼できないバイト列を
 * ObjectInputStream.readObject() でそのまま Java オブジェクトに戻していた。
 * これは「デシリアライズ（＝バイト列をオブジェクトに復元すること）」の中でも
 * 特に危険なやり方で、攻撃者が細工したバイト列を送ると、復元の過程で
 * 任意のプログラムを実行させられる恐れがある（リモートコード実行＝RCE の温床）。
 *
 * good 版では、信頼できない入力には Java ネイティブシリアライズを使わず、
 * JSON（Jackson）で「復元先の型を Cart に限定して」戻す。
 * JSON は「ただのデータ」であり、復元の過程で任意のコードが動くことはない。
 */
@Service
public class CartRestoreService {

    // JSON <-> オブジェクトの変換器。スレッドセーフなので使い回してよい
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ブラウザから受け取った JSON 文字列から Cart を復元して返す。
     *
     * @param cartJson カート状態を表す JSON 文字列（信頼できない外部入力）
     * @return 復元した Cart
     */
    public Cart restoreCart(String cartJson) {
        // 入力が空なら、はっきり例外で知らせる（bad 版のように null を返して握りつぶさない）
        if (cartJson == null || cartJson.isBlank()) {
            throw new IllegalArgumentException("カート情報が指定されていません");
        }
        try {
            // 復元先の型を Cart.class に固定する。
            // JSON はコードではなくデータなので、任意コード実行の入口にならない。
            // また Cart に無いフィールドが混ざっていても、Cart の形に収まる分だけが読まれる。
            return objectMapper.readValue(cartJson, Cart.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // 壊れた JSON が来たら、原因を握りつぶさず意味のある例外に変換して投げ直す。
            // 元の例外 e を原因として保持するので、ログで根本原因まで追える
            throw new IllegalArgumentException("カート情報の形式が不正です", e);
        }
    }
}

/*
 * ------------------------------------------------------------------
 * 【補足】どうしても Java ネイティブシリアライズを使わざるを得ない場合
 * ------------------------------------------------------------------
 * 外部システムの都合などで ObjectInputStream を避けられないときは、
 * 「復元してよいクラスのホワイトリスト」を ObjectInputFilter で必ず設定する。
 * こうすると許可した型以外は復元前に拒否され、攻撃用クラスの復元を防げる。
 * さらに try-with-resources でストリームを確実に閉じ、例外も握りつぶさない。
 *
 *   public Cart restoreCartFromBytes(byte[] data) {
 *       // try-with-resources：try(...) 内で開いたストリームは、
 *       // 正常時も例外時も自動で close される（リーク防止）
 *       try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
 *            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais)) {
 *
 *           // 復元してよい型だけを許可し、それ以外は REJECTED（拒否）する
 *           ois.setObjectInputFilter(info -> {
 *               Class<?> clazz = info.serialClass();
 *               if (clazz == null || clazz == Cart.class) {
 *                   return java.io.ObjectInputFilter.Status.ALLOWED;
 *               }
 *               return java.io.ObjectInputFilter.Status.REJECTED;
 *           });
 *
 *           Object obj = ois.readObject();
 *           // 戻り値の型を無検査でキャストせず、instanceof で確かめてから扱う
 *           if (!(obj instanceof Cart cart)) {
 *               throw new IllegalArgumentException("カート以外のデータが渡されました");
 *           }
 *           return cart;
 *
 *       } catch (java.io.IOException | ClassNotFoundException e) {
 *           // 例外の種類ごとに意味のある形で扱い、原因も保持する
 *           throw new IllegalArgumentException("カート情報の復元に失敗しました", e);
 *       }
 *   }
 */
