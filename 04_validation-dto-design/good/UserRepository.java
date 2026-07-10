package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;

// User エンティティのCRUD（保存・取得など）を提供するリポジトリ。
// JpaRepository を継承するだけで findById / save などが自動で使える。
public interface UserRepository extends JpaRepository<User, Long> {
}
