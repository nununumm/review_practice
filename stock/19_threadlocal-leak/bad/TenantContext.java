package com.example.tenant;

/**
 * 現在アクセス中のテナントID（＝どの会社の処理か）を保持するクラス。
 * ThreadLocal に入れておき、アプリのどこからでも取り出せるようにしている。
 */
public class TenantContext {

    // スレッドごとに別々の値を持てる保管箱。ここに現在のテナントIDを入れる。
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    // テナントIDをセットする
    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    // 現在のテナントIDを取り出す
    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }
}
