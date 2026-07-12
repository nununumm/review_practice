package com.example.shop.service;

import org.junit.jupiter.api.Test;                       // 「これはテストメソッドだよ」の目印
import static org.junit.jupiter.api.Assertions.assertEquals; // 期待値と実際値が一致するか確かめる命令

import java.time.Clock;
import java.time.Instant;      // ある“瞬間”を表す型（時計を固定するのに使う）
import java.time.ZoneOffset;   // タイムゾーン（ここではUTC=世界標準時で固定して結果をブレさせない）

/**
 * PriceService の単体テスト（JUnit 5）。
 *
 * ★ここが今回いちばん見てほしいところ★
 * good版は「時刻・セール・会員ランク」を外から渡せるので、
 * DBもサーバも起動せず、この1ファイルだけで“深夜割増・セール・GOLD割引”を全部検証できる。
 * bad版ではこれが書けなかった＝それが「テストしにくい設計」の正体だった。
 *
 * 補足：SaleRepository と MemberService はメソッドが1つだけの「関数型インターフェース」なので、
 * ラムダ式（() -> 値）で“その場で偽物”を作れる。Mockito などのライブラリすら不要。
 */
class PriceServiceTest {

    // 時計を「2026-07-12 23:00（＝深夜）」に固定するためのヘルパー
    private Clock fixedClockAt(int hour) {
        // 指定した時刻で1点に固定された、動かない時計を作る
        return Clock.fixed(Instant.parse("2026-07-12T" + String.format("%02d", hour) + ":00:00Z"),
                           ZoneOffset.UTC);
    }

    @Test
    void 深夜は10パーセント割増になる() {
        // 準備：23時固定・セールなし・一般会員、という状況を“外から”組み立てる
        PriceService service = new PriceService(
                fixedClockAt(23),          // 深夜23時の時計を注入
                () -> false,               // 「セール中？」→ false（偽物のSaleRepository）
                () -> "NORMAL");           // 「会員ランク？」→ NORMAL（偽物のMemberService）

        // 実行＆検証：1000円 → 深夜割増10% → 1100円のはず
        assertEquals(1100, service.calculatePrice(1000));
    }

    @Test
    void セール中は20パーセント割引になる() {
        // 昼14時・セールあり・一般会員
        PriceService service = new PriceService(
                fixedClockAt(14),          // 昼なので深夜割増はかからない
                () -> true,                // セール中！
                () -> "NORMAL");

        // 1000円 → セール20%オフ → 800円のはず
        assertEquals(800, service.calculatePrice(1000));
    }

    @Test
    void GOLD会員は100円引きになる() {
        // 昼14時・セールなし・GOLD会員
        PriceService service = new PriceService(
                fixedClockAt(14),
                () -> false,
                () -> "GOLD");             // GOLD会員！

        // 1000円 → GOLD割引100円 → 900円のはず
        assertEquals(900, service.calculatePrice(1000));
    }

    @Test
    void 昼の一般会員はベース価格のまま() {
        // 何の割増・割引も効かない“素の”ケースも必ず確認しておく
        PriceService service = new PriceService(
                fixedClockAt(14),
                () -> false,
                () -> "NORMAL");

        assertEquals(1000, service.calculatePrice(1000));
    }
}
