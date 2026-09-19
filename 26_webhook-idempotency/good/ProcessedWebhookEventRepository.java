package com.example.payment;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 処理済みイベント記録の入出力を担うリポジトリ。
 * Spring Data JPA が実装を自動生成するので、宣言だけでよい。
 */
public interface ProcessedWebhookEventRepository
        extends JpaRepository<ProcessedWebhookEvent, String> {
    // existsById(eventId) / save(...) は JpaRepository が標準で提供する
}
