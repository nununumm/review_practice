import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartCleanupService {

    private List<CartItem> cartItems = new ArrayList<>();

    public void addItem(CartItem item) {
        cartItems.add(item);
    }

    public void addItemByName(String name, int quantity) {
        CartItem found = null;
        for (CartItem item : cartItems) {
            if (item.getName().equals(name)) {
                found = item;
            }
        }
        cartItems.add(found);
    }

    public int cleanupUnavailable() {
        for (CartItem item : cartItems) {
            if (item.isSoldOut() || item.getStock() <= 0) {
                cartItems.remove(item);
            }
        }
        int totalQuantity = 0;
        for (CartItem item : cartItems) {
            totalQuantity += item.getQuantity();
        }
        return totalQuantity;
    }

    public int cleanupByIndex() {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).isSoldOut()) {
                cartItems.remove(i);
            }
        }
        int totalQuantity = 0;
        for (CartItem item : cartItems) {
            totalQuantity += item.getQuantity();
        }
        return totalQuantity;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }
}
