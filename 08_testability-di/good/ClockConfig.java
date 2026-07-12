package com.example.shop.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Spring は Clock を標準では用意してくれないので、ここで“本番用の時計”を登録しておく。
 *
 * ★なぜ必要？★
 * PriceService はコンストラクタで Clock を受け取る設計にした。
 * 本番では「実際の現在時刻を刻む時計」を差し込みたい。それをここで Bean（＝Springが管理する部品）として登録する。
 * テストのときは、この Bean を使わず、テストコード側で“固定時計”を new して直接渡せばよい。
 */
@Configuration   // このクラスが「Beanの登録場所」であることを示す注釈
public class ClockConfig {

    @Bean   // このメソッドの戻り値をSpringが部品として管理し、必要な場所（PriceService）に自動で注入する
    public Clock clock() {
        // システムの既定タイムゾーンで“現在時刻”を刻む本番用の時計
        return Clock.systemDefaultZone();
    }
}
