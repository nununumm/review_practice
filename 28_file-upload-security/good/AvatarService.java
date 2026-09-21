import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class AvatarService {

    // ロガー（System.out や printStackTrace ではなく、正式なログ基盤に出す）
    private static final Logger log = LoggerFactory.getLogger(AvatarService.class);

    // Content-Type → 付け直す拡張子の対応表（許可リストと対で使う）
    private static final Map<String, String> EXTENSION_BY_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png"
    );

    // 依存はすべてコンストラクタで受け取り、final で固定する（DI・テスト容易性）
    private final UserRepository userRepository;
    private final AvatarStorageProperties properties;

    public AvatarService(UserRepository userRepository, AvatarStorageProperties properties) {
        this.userRepository = userRepository;
        this.properties = properties;
    }

    /**
     * プロフィール画像をアップロードし、保存したファイル名をユーザーに紐づける。
     *
     * @return クライアントに返す「公開用ファイル名」。サーバー内部の絶対パスは返さない。
     */
    @Transactional // DB更新（save）を1つのトランザクションにまとめる
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 入力の検証（外部から来る値は信用しない）
        validate(file);

        // 2. 基準フォルダを「揺れのない絶対パス」に固める（.. や . を解決）
        Path baseDir = Paths.get(properties.getBaseDir()).toAbsolutePath().normalize();

        // 3. ユーザーのファイル名は使わず、自前で安全な名前を生成する
        //    → パストラバーサル(../)も、他人ファイルの上書き衝突も同時に防げる
        String newFileName = UUID.randomUUID() + resolveExtension(file.getContentType());

        // 4. 保存先を組み立て、正規化した実体パスが基準フォルダ内かを最終確認（多層防御）
        Path target = baseDir.resolve(newFileName).normalize();
        if (!target.startsWith(baseDir)) {
            throw new InvalidFileException("保存先が不正です");
        }

        // 5. 先にファイルを保存する（DBより先。DB失敗時は孤児ファイルで済み、リンク切れを避ける）
        storeFile(file, baseDir, target);

        // 6. 最後にDBを更新する。失敗したら、直前に置いたファイルを消して後始末（補償処理）
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new InvalidFileException("ユーザーが存在しません: " + userId));
            user.setAvatarPath(newFileName); // DBには内部絶対パスでなく「ファイル名」だけ持たせる
            userRepository.save(user);
        } catch (RuntimeException e) {
            deleteQuietly(target); // DB更新に失敗したので、置いたファイルを消す
            throw e;               // 例外はそのまま上へ伝える（握りつぶさない）
        }

        return newFileName; // 内部パスは隠し、公開用の名前だけ返す（情報漏洩を防ぐ）
    }

    /** 入力ファイルの検証：存在・サイズ・タイプを許可リストで確認する */
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {           // null・空(0バイト)を弾く（NPE/空ファイル防止）
            throw new InvalidFileException("ファイルが空です");
        }
        if (file.getSize() > properties.getMaxSizeBytes()) { // サイズ上限（ディスク枯渇・DoS防止）
            throw new InvalidFileException("ファイルサイズが大きすぎます");
        }
        String contentType = file.getContentType();
        if (contentType == null || !properties.getAllowedContentTypes().contains(contentType)) {
            // 「許可したものだけ通す」方式。禁止リストは抜け漏れるので使わない
            throw new InvalidFileException("画像はJPEG/PNGのみアップロードできます");
        }
    }

    /** Content-Type から安全な拡張子を決める（ユーザー名の拡張子は信用しない） */
    private String resolveExtension(String contentType) {
        return EXTENSION_BY_TYPE.getOrDefault(contentType, "");
    }

    /** ファイル本体をディスクへ書き出す。try-with-resources で確実に閉じる */
    private void storeFile(MultipartFile file, Path baseDir, Path target) {
        try {
            Files.createDirectories(baseDir); // 保存フォルダが無ければ作る
            // in・out とも try(...) の中で開くので、成功・例外どちらでも自動で閉じる（リーク防止）
            try (InputStream in = file.getInputStream();
                 OutputStream out = Files.newOutputStream(target)) {
                in.transferTo(out); // 1024バイトずつの手書きループの代わり。中身をまるごとコピー
            }
        } catch (IOException e) {
            // 保存に失敗。原因(e)を握りつぶさず、ログに残したうえで例外にラップして投げる
            log.error("アバターの保存に失敗しました. userId周辺の情報はログに残さない", e);
            throw new InvalidFileException("ファイルの保存に失敗しました");
        }
    }

    /** 後始末用：ファイル削除。削除自体が失敗しても本筋の例外を消さないよう握って警告ログのみ */
    private void deleteQuietly(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("孤児ファイルの削除に失敗しました: {}", target, e);
        }
    }
}
