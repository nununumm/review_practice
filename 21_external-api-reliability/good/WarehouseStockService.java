package com.example.stock;

import org.springframework.beans.factory.annotation.Value; // 設定ファイルの値を注入する印
import org.springframework.stereotype.Service;             // サービス層のBeanにする印
import org.springframework.web.client.RestClientException;  // HTTP呼び出し失敗(タイムアウト含む)を表す例外
import org.springframework.web.client.RestTemplate;         // HTTPで外部を呼ぶ道具（タイムアウト設定済みのものが注入される）

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 注文された全商品について、外部倉庫システム（WMS）に在庫を問い合わせるサービス（改善版）。
 *
 * bad版からの主な改善：
 *  - RestTemplate を new せず、タイムアウト設定済みのものを DI で受け取る（テスト可能・待ち過ぎ防止）
 *  - 商品ごとの逐次呼び出しをやめ、全商品を「1回」の一括問い合わせにまとめる（外部API版N+1の解消）
 *  - 例外を握りつぶさず、専用例外で上位へ伝える（原因を失わない）
 *  - 「在庫不足」と「確認失敗」を別状態として区別する（黙って捨てない）
 *  - URL は設定ファイルから注入（環境ごとに切替可能）／値は本文に載せて送る（文字列連結しない）
 */
@Service
public class WarehouseStockService {

    private final RestTemplate restTemplate; // タイムアウト済みのHTTP道具（DIで注入）
    private final String wmsBulkUrl;         // WMSの一括問い合わせURL（設定ファイルから注入）

    // コンストラクタで依存を受け取る＝テスト時に偽物へ差し替え可能／URLも外部化
    public WarehouseStockService(RestTemplate restTemplate,
                                 @Value("${wms.stock-bulk-url}") String wmsBulkUrl) {
        this.restTemplate = restTemplate;
        this.wmsBulkUrl = wmsBulkUrl;
    }

    public List<StockResult> checkStock(List<OrderItem> items) {
        // 入力が空なら外部を呼ぶ必要なし（無駄な通信を避ける）
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        // 1) 全商品を1つのリクエストにまとめる（1商品=1リクエストの逐次呼び出しをやめる）
        WmsBulkRequest request = new WmsBulkRequest(
                items.stream()
                        .map(item -> new WmsItem(item.getProductCode()))
                        .collect(Collectors.toList())
        );

        // 2) 一括で1回だけ呼ぶ。タイムアウトは注入された RestTemplate 側で効く
        WmsBulkResponse response;
        try {
            response = restTemplate.postForObject(wmsBulkUrl, request, WmsBulkResponse.class);
        } catch (RestClientException e) {
            // タイムアウト／接続不可／相手のエラー応答など。握りつぶさず原因ごと上位へ投げる
            throw new StockCheckException("WMSへの在庫確認に失敗しました", e);
        }

        // 3) 例外は出ていないが中身が空（null）のケースも「確認できなかった」異常として扱う
        if (response == null || response.entries() == null) {
            throw new StockCheckException("WMSから有効な応答が返りませんでした", null);
        }

        // 4) レスポンスを「商品コード → 在庫数」で引けるMapに変換（照合しやすくする）
        Map<String, Integer> stockByCode = response.entries().stream()
                .collect(Collectors.toMap(WmsStockEntry::code, WmsStockEntry::availableQuantity));

        // 5) 注文の各商品を、確実な結果として詰め直す（1件も取りこぼさない）
        return items.stream()
                .map(item -> toResult(item, stockByCode))
                .collect(Collectors.toList());
    }

    /** 1商品ぶんの在庫数を、確実な状態(StockResult)に変換する */
    private StockResult toResult(OrderItem item, Map<String, Integer> stockByCode) {
        Integer available = stockByCode.get(item.getProductCode());

        // 依頼したのにWMSが返してこなかった商品＝確認できていない。黙って捨てず CHECK_FAILED にする
        if (available == null) {
            return new StockResult(item.getProductCode(), StockStatus.CHECK_FAILED, 0);
        }

        // 在庫数と必要数を比べて「引当可能」か「在庫不足」かを判定
        StockStatus status = (available >= item.getCount())
                ? StockStatus.AVAILABLE
                : StockStatus.SHORTAGE;
        return new StockResult(item.getProductCode(), status, available);
    }
}
