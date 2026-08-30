package com.example.member.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;        // @Async を有効化する印
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 非同期処理（@Async）が使う「スレッドプール」の設定。
 *
 * bad版の new Thread(...).start() は、呼ばれるたびにスレッドを新品で作って使い捨てていた。
 * 1秒に100件来ると毎秒100本のスレッドが生まれ、上限がないためメモリ枯渇・サーバ停止につながる。
 *
 * ここでは「固定人数のチーム＋順番待ちの列」を用意する：
 *  - corePoolSize/maxPoolSize … 同時に働くスレッドの人数（上限あり）
 *  - queueCapacity           … 手が空くまで仕事を並べておく列の長さ
 *  - CallerRunsPolicy        … 列も一杯なら、依頼元自身に処理させて"流入にブレーキ"をかける（背圧）
 * これにより、急な負荷でもスレッドが無制限に増えず、サーバが守られる。
 */
@Configuration
@EnableAsync // これがないと @Async は何も起きない（ただのメソッド呼び出しになる）
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // 常に待機させておく人数
        executor.setMaxPoolSize(10);      // 混雑時に増やせる最大人数（＝ここで頭打ち）
        executor.setQueueCapacity(100);   // 全員ふさがっているときに並べておける仕事の数
        executor.setThreadNamePrefix("member-async-"); // ログで追いやすいようスレッドに名前を付ける
        // 人も列も一杯になったら、依頼したスレッド自身に実行させる＝これ以上受けないブレーキ
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
