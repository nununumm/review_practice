package com.example.member;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 会員データにアクセスするリポジトリ。
 * Spring Data JPA の JpaRepository を継承しているので、
 * findById(id) は「見つかればその会員、見つからなければ空」を表す Optional を返す。
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
}
