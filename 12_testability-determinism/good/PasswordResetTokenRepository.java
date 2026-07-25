package com.example.auth;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 再設定トークンを保存・検索するリポジトリ（＝DBとのやり取り担当）。
 *
 * ★ユーザーの指摘「現在時刻→保存の隙間を @Query の独自SQLで埋める」について★
 *   気持ちは分かるが、有効期限の“計算”を SQL（DB側の NOW() など）に寄せるのは今回は避ける。理由は2つ。
 *     (1) テスト容易性が下がる … DB の時刻はJUnitから固定できないため、
 *         「有効期限は本当に1時間後か？」を検証できなくなる。今回はここが主眼。
 *         → 時刻は Java 側の Clock で扱い、固定できる状態を保つのが正解。
 *     (2) そもそも今回の処理は「新規発行して save するだけ」で、他者と取り合う競合は無い。
 *         “隙間（=わずかな時間差）”がバグになるのは、
 *         「DBの値を読んで → 判断して → 書き戻す」という Read-Modify-Write のとき。
 *
 *   では @Query（＝独自SQL）が活きるのはどこか？ → 例えば「期限切れトークンの掃除」など、
 *   “DBの現在時刻で一括判定したい”処理。下に一例を挙げておく（今回の requestReset では使わない）。
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // 参考例：期限切れトークンをまとめて削除する用途なら独自SQLが妥当。
    //         （ここでの :now も、呼び出し側で clock.instant() を渡せばテストで固定できる）
    // @Modifying
    // @Query("delete from PasswordResetToken t where t.expiresAt < :now")
    // int deleteExpired(@Param("now") Instant now);
}
