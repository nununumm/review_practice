package com.example.demo.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 【Service層】業務ロジックの本体。Controllerから処理を委譲される。
// ここに集約しておくと、テストしやすく（Controllerを介さず単体テスト可能）、
// 他の画面やAPIからも同じ処理を再利用できる。
@Service
public class UserService {

    private final UserRepository userRepository;

    // コンストラクタインジェクション（＝DI：依存を外から注入する）。
    // これにより UserService は UserRepository と疎結合になり、テスト時に差し替えやすい。
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // プロフィール取得。参照だけなので readOnly=true（読み取り専用トランザクション）。
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        // .get() ではなく orElseThrow を使い、無ければ意味のある例外を投げる。
        // → GlobalExceptionHandler で 404 に変換される。
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // プロフィール更新。1回の呼び出しを1トランザクションにまとめる。
    @Transactional
    public User updateProfile(Long id, UserUpdateRequest request) {
        // まず対象ユーザーを取得（無ければ例外）。
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // ★更新してよい項目だけを、明示的に上書きする。
        //   request には name と email しか無いので、role や password は
        //   このメソッドからは絶対に書き換わらない（マスアサインメント対策）。
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // JPA管理下のエンティティなので save は省略もできるが、
        // 「保存する」という意図を明示するため呼んでおく。
        return userRepository.save(user);
    }
}
