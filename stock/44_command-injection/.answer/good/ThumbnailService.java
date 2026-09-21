package good;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * アップロード PDF のサムネイル生成サービス（修正版）。
 *
 * bad 版は、ユーザー由来のファイル名を「シェルに渡す1本の文字列コマンド」に
 * そのまま連結していた。これはコマンドインジェクション（＝入力に紛れ込ませた
 * OS コマンドを実行させる攻撃）の典型で、たとえば fileName に
 *   "a.pdf; rm -rf /var"
 * のような値を入れられると、サーバー上で任意のコマンドを実行されてしまう。
 *
 * good 版の要点：
 *  1) シェル(sh -c)を介さず、コマンドと引数を「配列」で分けて渡す（文字列連結しない）
 *  2) ファイル名を検証し、パス区切りや親ディレクトリ参照(..)を許さない
 *  3) 標準出力・標準エラーを消費し、タイムアウトも設ける（ハング・ゾンビ防止）
 */
public class ThumbnailService {

    private final Path uploadDir;
    private final Path thumbDir;

    public ThumbnailService(Path uploadDir, Path thumbDir) {
        this.uploadDir = uploadDir;
        this.thumbDir = thumbDir;
    }

    public void createThumbnail(String fileName) {
        // 1) 入力検証：英数字・ハイフン・アンダースコアと .pdf のみ許可。
        //    これで "; rm -rf" や "../../etc/passwd" のような値は入口で弾ける。
        if (fileName == null || !fileName.matches("[A-Za-z0-9_-]+\\.pdf")) {
            throw new IllegalArgumentException("不正なファイル名です: " + fileName);
        }

        // 2) パスは「ディレクトリ + ファイル名」を resolve で安全に合成する（文字列連結しない）。
        Path input = uploadDir.resolve(fileName);
        Path output = thumbDir.resolve(fileName);

        try {
            // 3) ProcessBuilder に「コマンドと引数を1個ずつ」配列で渡す。
            //    シェルを経由しないので、引数の中に ; や | があっても「ただの文字列」として扱われ、
            //    コマンドとして解釈されることはない＝インジェクションが成立しない。
            ProcessBuilder pb = new ProcessBuilder(
                    "pdftoppm", "-png",
                    input.toString(),
                    output.toString());
            // 標準エラーを標準出力にまとめ、下で読み切る（バッファ詰まりでのハングを防ぐ）
            pb.redirectErrorStream(true);

            Process p = pb.start();
            // 出力を読み捨てておかないと、出力バッファが一杯になった時点でプロセスが止まる
            p.getInputStream().readAllBytes();

            // 無限待ちを避け、一定時間で見切りをつける
            boolean finished = p.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                throw new IllegalStateException("サムネイル生成がタイムアウトしました: " + fileName);
            }
            if (p.exitValue() != 0) {
                throw new IllegalStateException("サムネイル生成に失敗しました (exit=" + p.exitValue() + "): " + fileName);
            }

        } catch (IOException e) {
            // 握りつぶさず、原因を包んで投げ直す
            throw new IllegalStateException("サムネイル生成でI/Oエラー: " + fileName, e);
        } catch (InterruptedException e) {
            // 割り込みは握りつぶさず、割り込み状態を復元してから通知する
            Thread.currentThread().interrupt();
            throw new IllegalStateException("サムネイル生成が中断されました: " + fileName, e);
        }
    }
}
