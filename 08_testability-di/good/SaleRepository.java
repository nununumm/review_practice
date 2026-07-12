package com.example.shop.service;

/**
 * 「今タイムセール中か？」を答えてくれる“窓口”（インターフェース）。
 *
 * ★ポイント★
 * bad版では PriceService の中で `new SaleRepository()` と本物を直接作っていた。
 * ここを「インターフェース（＝約束事だけ決めた型）」にしておくと、
 *   ・本番 … 本物のDB実装を差し込む
 *   ・テスト … 「常に true を返す偽物」を差し込む
 * というふうに“中身を差し替え”できるようになる。これがテストしやすさの鍵。
 */
public interface SaleRepository {

    // 今セール中なら true、そうでなければ false を返す（実装は本番用/テスト用で差し替え可能）
    boolean isSaleNow();
}
