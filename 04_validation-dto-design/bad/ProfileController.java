package com.example.demo.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// ユーザーのプロフィール（名前・メール）を参照／更新するAPI
@RestController
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // プロフィール取得API
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).get();
    }

    // プロフィール更新API
    // フロントから送られてきたユーザー情報で上書き保存する
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User request) {
        User user = userRepository.findById(id).get();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        return userRepository.save(user);
    }
}
