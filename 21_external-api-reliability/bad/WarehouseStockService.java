package com.example.stock;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 注文された各商品について、外部倉庫システム（WMS）のREST APIを呼び出し、
 * 「その商品がいくつ在庫として引き当て可能か」を問い合わせるサービス。
 *
 * 画面の「在庫確認」ボタンから呼ばれ、注文に含まれる全商品の在庫状況を
 * まとめて返す想定。
 */
@Service
public class WarehouseStockService {

    // 外部倉庫システムのエンドポイント
    private static final String WMS_URL = "http://wms.example.com/api/stock";

    public List<StockResult> checkStock(List<OrderItem> items) {
        List<StockResult> results = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (OrderItem item : items) {
            try {
                // 商品コードと必要数をクエリにつなげて在庫を問い合わせる
                String url = WMS_URL + "?code=" + item.getProductCode()
                        + "&need=" + item.getCount();

                StockResponse response = restTemplate.getForObject(url, StockResponse.class);

                StockResult result = new StockResult();
                result.setProductCode(item.getProductCode());
                result.setAvailable(response.getAvailableQuantity() >= item.getCount());
                results.add(result);

            } catch (Exception e) {
                // 呼び出しに失敗した商品はスキップして次へ
                continue;
            }
        }

        return results;
    }
}
