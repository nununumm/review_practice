package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;

// User エンティティのCRUDを提供するリポジトリ
public interface UserRepository extends JpaRepository<User, Long> {
}
