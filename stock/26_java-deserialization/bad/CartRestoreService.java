package bad;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

@Service
public class CartRestoreService {

    /**
     * Cookie やリクエストパラメータで受け取った Base64 文字列から
     * カート状態（Cart）を復元して返す。
     */
    public Cart restoreCart(String encodedCart) {
        try {
            byte[] data = Base64.getDecoder().decode(encodedCart);

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);

            Cart cart = (Cart) ois.readObject();

            return cart;
        } catch (Exception e) {
            return null;
        }
    }
}
