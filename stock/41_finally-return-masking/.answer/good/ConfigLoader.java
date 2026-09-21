package good;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 設定ファイルから API キーを読み込むサービス（修正版）。
 *
 * bad 版の最大の罠は finally の中に return があったこと。
 *  ・finally は「try/catch を抜ける直前に必ず通る後片付け部屋」。ここに return を書くと、
 *    本体（try）が返そうとしていた値も、投げようとしていた例外も、finally の return が
 *    上書きして握りつぶしてしまう。bad 版は何が起きても最終的に "" を返すため、
 *    呼び出し側は「読み込み成功で空文字」なのか「失敗」なのか永遠に区別できなかった。
 *  ・さらに BufferedReader を手動 close していたため finally が入れ子で読みづらく、
 *    catch(Exception) で握りつぶして null を返し、原因(e)も失われていた。
 *
 * good 版のポイント：
 *  1) try-with-resources で自動 close（finally 手書きを消す）
 *  2) finally に return を書かない（値も例外も握りつぶさない）
 *  3) 失敗は原因(e)付きで上位に投げ、呼び出し側が気づけるようにする
 *  4) ファイルパスは外から渡す・文字コードは明示する
 */
public class ConfigLoader {

    // パスをハードコードせず外から受け取る（環境ごとに差し替え可能・テストしやすい）
    private final Path configPath;

    public ConfigLoader(Path configPath) {
        this.configPath = configPath;
    }

    public String loadApiKey() {
        // try-with-resources：( ) の中で開いたリソースは、正常終了でも例外でも自動で close される。
        // finally を自分で書く必要がなくなり、「finally の return で握りつぶす」事故も起きない。
        // 文字コード(UTF-8)を明示：環境依存の文字化けを防ぐ。
        try (BufferedReader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            Properties props = new Properties();
            props.load(reader);

            String apiKey = props.getProperty("api.key");
            // 「見つからない」を空文字や null でごまかさず、はっきり異常として扱う
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("api.key が設定ファイルにありません: " + configPath);
            }
            return apiKey;

        } catch (IOException e) {
            // 握りつぶさない：原因(e)を包んで投げ直す。呼び出し側はスタックトレースで原因を追える。
            throw new UncheckedIOException("設定ファイルの読み込みに失敗しました: " + configPath, e);
        }
    }
}
