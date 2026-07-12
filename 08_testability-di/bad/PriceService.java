package com.example.shop.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 商品の「支払い金額」を計算するサービス。
 *
 * ベース価格に対して、
 *   ・深夜(22時〜翌5時)なら「深夜割増」
 *   ・タイムセール中なら「セール割引」
 *   ・GOLD会員なら「会員割引」
 * を順番に適用して、最終的な支払い金額を返す。
 */
@Service
public class PriceService {

    /**
     * ベース価格を受け取り、各種割増・割引を適用した支払い金額を返す。
     *
     * @param basePrice 商品のベース価格（円）
     * @return 実際に支払う金額（円）
     */
    public int calculatePrice(int basePrice) {
        // 今の時刻を取得する
        LocalDateTime now = LocalDateTime.now();
        int price = basePrice;

        // 深夜(22時以降 または 5時より前)は割増10%
        LocalTime time = now.toLocalTime();
        if (time.isAfter(LocalTime.of(22, 0)) || time.isBefore(LocalTime.of(5, 0))) {
            price = price + (price / 10);
        }

        // 今タイムセール中かどうかをDBの設定から確認する
        SaleRepository saleRepository = new SaleRepository();
        if (saleRepository.isSaleNow()) {
            // セール中は20%オフ
            price = price - (price / 5);
        }

        // ログイン中ユーザーの会員ランクを取得する
        String rank = MemberUtil.getCurrentRank();
        if (rank.equals("GOLD")) {
            // GOLD会員は一律100円引き
            price = price - 100;
        }

        // 計算結果を出力しておく
        System.out.println("最終価格: " + price);

        return price;
    }
}
