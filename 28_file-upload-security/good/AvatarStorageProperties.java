import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * アバター保存に関する「設定値」をまとめて外部化するためのクラス。
 *
 * ハードコード（コードに直書き）をやめ、application.yml 等から値を注入する。
 * こうすると、開発環境と本番環境、Windows と Linux で保存先を切り替えられる。
 *
 * application.yml 側の記述例:
 *   avatar:
 *     base-dir: /var/www/uploads/avatars   # 保存先フォルダ
 *     max-size-bytes: 5242880              # 5MB（サイズ上限）
 *     allowed-content-types:               # 許可する画像タイプ（許可リスト方式）
 *       - image/jpeg
 *       - image/png
 */
@Component
@ConfigurationProperties(prefix = "avatar") // "avatar.xxx" の設定をこのクラスに流し込む
public class AvatarStorageProperties {

    /** 保存先の基準フォルダ（ここより外には絶対に書き込ませない） */
    private String baseDir;

    /** 1ファイルあたりの最大サイズ（バイト）。巨大ファイルによるディスク枯渇を防ぐ */
    private long maxSizeBytes;

    /** 許可する Content-Type の集合。ここに無いものは弾く（禁止リストでなく許可リスト） */
    private Set<String> allowedContentTypes;

    // 以下は Spring が設定値を流し込む／読み出すための getter/setter
    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public Set<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(Set<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }
}
