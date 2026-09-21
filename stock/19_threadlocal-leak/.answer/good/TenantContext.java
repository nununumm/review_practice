package com.example.tenant;

import java.util.Optional;

/**
 * 現在アクセス中のテナントID（＝どの会社の処理か）を、リクエスト処理中だけ一時的に保持するクラス。
 *
 * ThreadLocal（＝スレッドごとに別々の値を持てる保管箱）は便利だが、Webサーバはスレッドを
 * 使い回す（＝スレッドプール）ので、「入れたら必ず片付ける」ことが絶対条件になる。
 * このクラスは set / clear をペアで持ち、片付けを呼び出し側から必ず実行できるようにしている。
 */
public final class TenantContext {

    // スレッドごとに別々の値を持てる保管箱。現在のテナントIDを入れる。
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    // ユーティリティクラスなので、外からインスタンス化させない
    private TenantContext() {
    }

    /** テナントIDをセットする（リクエストの入口で1回だけ呼ぶ想定） */
    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * 現在のテナントIDを取り出す。
     * まだセットされていない（＝テナント不明）なら、黙って全件にせず例外で気づかせる。
     */
    public static Long requireTenantId() {
        Long tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            // 「テナント不明なのにデータを触ろうとした」＝バグや設定漏れなので、はっきり落とす
            throw new IllegalStateException("テナントIDが未設定です。入口でセットされているか確認してください。");
        }
        return tenantId;
    }

    /**
     * 「あるかもしれない」を型で表して安全に受け渡すための取り出し口（＝Optional）。
     * 別スレッドへ値を明示的に渡したいときなどに使う。
     */
    public static Optional<Long> currentTenantId() {
        return Optional.ofNullable(CURRENT_TENANT.get());
    }

    /**
     * 保管箱を空にする（＝片付け）。
     * これを finally で必ず呼ぶことで、スレッドが使い回されても前のリクエストの
     * テナントIDが残らない（＝別会社のデータが見える事故／メモリリークを防ぐ）。
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
