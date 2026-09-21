import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.InputStream;

@Service
public class AvatarService {

    private final UserRepository userRepository;

    public AvatarService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // プロフィール画像をアップロードして、保存先パスをユーザーに紐づける
    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            // アップロードされたファイル名をそのまま使って保存先を組み立てる
            String savePath = "/var/www/uploads/avatars/" + file.getOriginalFilename();

            InputStream in = file.getInputStream();
            FileOutputStream out = new FileOutputStream(savePath);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.close();

            // ユーザーの画像パスを更新する
            User user = userRepository.findById(userId).get();
            user.setAvatarPath(savePath);
            userRepository.save(user);

            return savePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
