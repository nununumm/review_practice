import java.io.BufferedReader;          // 1行ずつ効率よく読むための道具
import java.io.IOException;             // ファイル読み込み中に起きうる例外
import java.nio.charset.StandardCharsets; // 文字コード（UTF-8 など）を明示するための定数
import java.nio.file.Files;            // ファイルを開く近代的なユーティリティ
import java.nio.file.Path;             // ファイルの場所を表す型（Stringより安全）
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品マスタのCSVファイルを取り込むサービス（模範解答）。
 *
 * 直したポイントの全体像：
 *  1. try-with-resources で「開いたファイルは必ず閉じる」
 *  2. 全行をメモリに貯めず「1行ずつ」処理する（大容量でも落ちない）
 *  3. @Transactional で「全部成功か、全部なかったことにするか」を保証する
 *  4. saveAll で「まとめて登録」してDBアクセス回数を減らす
 *  5. 例外は握りつぶさず、行番号つきで投げて呼び出し側に伝える
 *  6. ヘッダ行のスキップ・列数/数値/負数のチェックを入れる
 */
@Service
@RequiredArgsConstructor
public class ProductImportService {

    private final ProductRepository productRepository;

    // 一度の saveAll でまとめて登録する件数。メモリ使用量とDB効率のバランスをとる値。
    private static final int BATCH_SIZE = 1000;
    // CSV1行の列数（商品コード, 商品名, 価格, 在庫数 の4列）。マジックナンバーを定数に。
    private static final int EXPECTED_COLUMNS = 4;

    /**
     * CSVを取り込み、登録した件数を返す。
     * 失敗した場合は CsvImportException を投げる（＝呼び出し側が原因を検知できる）。
     * このメソッド全体が1つのトランザクション。途中で例外が出れば、登録済みの分も自動で巻き戻る。
     */
    @Transactional
    public int importProducts(Path filePath) {
        List<Product> buffer = new ArrayList<>(); // まとめ登録用の一時バッファ（最大でもBATCH_SIZE件しか持たない）
        int totalCount = 0;                        // 登録した合計件数
        long lineNo = 0;                           // 現在の行番号（エラーメッセージ用）

        // ★ try-with-resources：() の中で開いたリソースは、正常終了でも例外でも“自動でclose()”される。
        //    → 例外が起きてもファイルが閉じ忘れられることがない（リソースリークを根本から防ぐ）。
        //    ★ 文字コードを UTF-8 と明示。環境依存の文字化けを防ぐ（元コードの FileReader は既定文字コード頼み）。
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {

            // 1行目はヘッダ（列名の行）なので読み飛ばす。データとして登録しない。
            String line = reader.readLine();
            lineNo++;

            // 2行目以降を「1行ずつ」処理する。全行をメモリに貯めないのでファイルが巨大でも大丈夫。
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue; // 空行は無視
                }

                // 1行をProductに変換。不正な行ならここで例外が飛び、トランザクションごと巻き戻る。
                buffer.add(parseLine(line, lineNo));

                // 一定件数たまったら、まとめてDBへ登録し、バッファを空にする（メモリを圧迫しない）。
                if (buffer.size() >= BATCH_SIZE) {
                    productRepository.saveAll(buffer); // 1件ずつではなく、まとめて登録
                    totalCount += buffer.size();
                    buffer.clear();
                }
            }

            // 端数（BATCH_SIZE に満たなかった残り）を最後に登録する。
            if (!buffer.isEmpty()) {
                productRepository.saveAll(buffer);
                totalCount += buffer.size();
            }

        } catch (IOException e) {
            // ここに来るのは「ファイルの読み込み」で失敗したケース。
            // DB登録の失敗（parseLine 内で投げる CsvImportException）と“区別できる”よう、
            // 読み込み失敗であることを明示したメッセージで包んで投げ直す（原因 e も捨てない）。
            throw new CsvImportException("CSVファイルの読み込みに失敗しました: " + filePath, e);
        }

        // ここまで来たら全件成功。件数は「実際にコミットされる件数」と一致する。
        return totalCount;
    }

    /**
     * CSVの1行を Product に変換する。あわせて入力チェックを行う。
     * 不正な行は CsvImportException（行番号つき）を投げて、取り込み全体を止める。
     */
    private Product parseLine(String line, long lineNo) {
        // 末尾の空フィールドも維持するため split の第2引数に -1 を指定。
        // ※注意：この単純な split は値の中にカンマを含むCSV（例 "1,000"）には対応できない。
        //   本番では OpenCSV / Apache Commons CSV などのCSVライブラリを使うのが定石（解説参照）。
        String[] cols = line.split(",", -1);

        // 列数チェック：足りない/多い行は不正データとしてはじく（元コードは cols[3] で配列外アクセスの危険）。
        if (cols.length != EXPECTED_COLUMNS) {
            throw new CsvImportException(
                    lineNo + "行目: 列数が不正です（期待=" + EXPECTED_COLUMNS + ", 実際=" + cols.length + "）");
        }

        String code = cols[0].trim();
        String name = cols[1].trim();
        if (code.isEmpty() || name.isEmpty()) {
            throw new CsvImportException(lineNo + "行目: 商品コード・商品名は必須です");
        }

        // 数値変換は失敗しうるので専用メソッドで安全に。負の価格・在庫も弾く。
        int price = parseNonNegativeInt(cols[2].trim(), lineNo, "価格");
        int stock = parseNonNegativeInt(cols[3].trim(), lineNo, "在庫数");

        Product product = new Product();
        product.setCode(code);
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        return product;
    }

    /**
     * 文字列を「0以上の整数」に変換する。数値でない・負の数のときは分かりやすい例外にする。
     * 元コードの Integer.parseInt(cols[2]) は、数値でない値が来ると生の NumberFormatException で落ち、
     * 「何行目のどの項目か」が分からなかった。ここではそれを行番号つきの例外に翻訳している。
     */
    private int parseNonNegativeInt(String value, long lineNo, String fieldName) {
        try {
            int num = Integer.parseInt(value);
            if (num < 0) {
                throw new CsvImportException(lineNo + "行目: " + fieldName + "が負の数です（" + value + "）");
            }
            return num;
        } catch (NumberFormatException e) {
            throw new CsvImportException(
                    lineNo + "行目: " + fieldName + "が数値ではありません（" + value + "）", e);
        }
    }
}
