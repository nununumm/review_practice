package com.example.stock;

import org.springframework.boot.web.client.RestTemplateBuilder; // タイムアウト等を設定してRestTemplateを組み立てる道具
import org.springframework.context.annotation.Bean;             // このメソッドの戻り値をSpring管理の部品(Bean)にする印
import org.springframework.context.annotation.Configuration;    // 設定用クラスであることを示す印
import org.springframework.web.client.RestTemplate;             // HTTPで外部を呼ぶ道具

import java.time.Duration; // 「2秒」などの時間を型安全に表す

/**
 * 「タイムアウトを設定済みの RestTemplate」を、アプリ全体で1個だけ用意する設定クラス。
 *
 * ここで一度だけ組み立ててBean化しておけば、各サービスはコンストラクタで
 * 受け取って(DI)使い回せる。タイムアウトの値もこの一箇所で管理できる。
 */
@Configuration
public class RestTemplateConfig {

    @Bean // 戻り値の RestTemplate を Spring に登録し、他クラスへ注入できるようにする
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                // 接続タイムアウト：相手サーバーに「つながる」までの上限（これを超えたら諦める）
                .setConnectTimeout(Duration.ofSeconds(2))
                // 読み取りタイムアウト：つながった後、「返事が来る」までの上限
                .setReadTimeout(Duration.ofSeconds(3))
                .build(); // 上記設定を反映した RestTemplate を組み立てて返す
    }
}
