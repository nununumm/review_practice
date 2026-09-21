package good;

import org.springframework.stereotype.Component;

/**
 * 送金内容の検証（修正版）。
 *
 * bad 版は入力チェックに assert を使っていた。ここが致命的。
 *   assert は「開発中に、起こり得ないはずの前提を確認する」ためのデバッグ用の仕掛けで、
 *   本番の Java 実行では標準で無効（起動時に -ea を付けない限り実行されない）。
 *   つまり本番では assert の行がまるごとスキップされ、
 *   マイナス送金・上限超え・不正な口座番号のチェックが「全部素通り」する。
 *   開発環境（-ea 付き）では動くので、本番でだけ検証が消える最悪のパターン。
 *
 * ユーザー入力など「外から来る値」の検証は、必ず通常の if＋例外で行う。
 */
@Component
public class TransferValidator {

    // 上限額はマジックナンバーにせず、意味の分かる定数にする（外部設定にしてもよい）
    private static final long MAX_AMOUNT = 1_000_000L;
    private static final int ACCOUNT_NUMBER_LENGTH = 10;

    public void validate(long amount, String toAccount) {
        // if＋例外で検証する。これは本番でも必ず実行される。
        if (amount <= 0) {
            throw new IllegalArgumentException("送金額は正の数でなければなりません: " + amount);
        }
        if (amount > MAX_AMOUNT) {
            throw new IllegalArgumentException("送金額が上限(" + MAX_AMOUNT + "円)を超えています: " + amount);
        }
        if (toAccount == null || toAccount.length() != ACCOUNT_NUMBER_LENGTH) {
            throw new IllegalArgumentException("口座番号は" + ACCOUNT_NUMBER_LENGTH + "桁で指定してください");
        }
        // 「10桁」だけでなく「数字のみか」も確認する（形式検証を一段厚くする）
        if (!toAccount.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("口座番号は数字のみで指定してください");
        }
    }
}
