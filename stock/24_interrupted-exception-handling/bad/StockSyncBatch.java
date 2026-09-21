package bad;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class StockSyncBatch {

    private final StockApiClient apiClient;

    public StockSyncBatch(StockApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void sync(List<StockItem> items) {
        for (StockItem item : items) {

            while (true) {
                try {
                    apiClient.push(item);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
    }
}

interface StockApiClient {
    void push(StockItem item);
}

class StockItem {
    private final String sku;
    private final int quantity;

    StockItem(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    String getSku() {
        return sku;
    }

    int getQuantity() {
        return quantity;
    }
}
