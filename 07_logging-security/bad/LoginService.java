package com.example.demo.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String email, String rawPassword) {
        logger.info("ログイン試行: email=" + email + ", password=" + rawPassword);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.info("ユーザーが存在しません: " + email);
            return false;
        }

        if (user.getPassword().equals(rawPassword)) {
            logger.info("ログイン成功。ユーザー情報=" + user);
            return true;
        } else {
            logger.error("パスワード不一致。 入力値=" + rawPassword
                    + " / 正しいパスワード=" + user.getPassword());
            return false;
        }
    }
}
