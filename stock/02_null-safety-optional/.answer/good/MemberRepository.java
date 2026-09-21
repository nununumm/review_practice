package com.example.member;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 会員データにアクセスするリポジトリ。（bad/ と同じ）
 * findById(id) は「見つかればその会員、見つからなければ空」を表す Optional を返す。
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
}
