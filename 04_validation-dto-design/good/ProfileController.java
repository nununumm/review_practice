package com.example.demo.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// 【Controller層】APIの「受付係」。
// 役割は「リクエストを受け取り、Serviceに渡し、結果をDTOで返す」だけに薄く保つ。
// 判断や業務ロジックはここには書かない。
@RestController
public class ProfileController {

    private final UserService userService;

    // Serviceを注入（DI）。Controllerは直接Repositoryを触らない。
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // プロフィール取得API
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        // Serviceから取得したエンティティを、返信用DTOに詰め替えて返す。
        // → password / role は UserResponse に含まれないので外に漏れない。
        User user = userService.getUser(id);
        return ResponseEntity.ok(new UserResponse(user));
    }

    // プロフィール更新API
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            // @RequestBody = リクエスト本文(JSON)を UserUpdateRequest に変換して受け取る。
            // @Valid       = そのDTOに付けた @NotBlank / @Email などのルールを自動チェックする。
            //                違反時は例外が飛び、GlobalExceptionHandler で 400 に変換される。
            //                （Springの @Validated を使っても同じくチェックが起動する）
            @Valid @RequestBody UserUpdateRequest request) {

        // 受け取った依頼をそのままServiceに渡すだけ。Controllerは判断しない。
        User updated = userService.updateProfile(id, request);

        // 更新後の状態も、必ず返信用DTOに詰め替えてから返す。
        return ResponseEntity.ok(new UserResponse(updated));
    }
}
