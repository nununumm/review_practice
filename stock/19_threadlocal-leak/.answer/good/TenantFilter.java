package com.example.tenant;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * リクエストの入口と出口を一元管理する Filter（＝全リクエストが必ず通る関所）。
 * ここで「入れる」と「片付ける」をワンセットにしておくことで、
 * 各サービスが個別に片付けを気にしなくてよくなる。
 */
@Component
public class TenantFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;

        // リクエストヘッダー "X-Tenant-Id" からテナントIDを取り出してセットする
        String header = http.getHeader("X-Tenant-Id");
        if (header != null) {
            TenantContext.setTenantId(Long.valueOf(header));
        }

        try {
            // 後続の処理（コントローラやサービス）へ処理を渡す。
            // ここで例外が飛んでも、下の finally が必ず実行される。
            chain.doFilter(request, response);
        } finally {
            // ★最重要★ 正常でも異常（例外）でも、必ず保管箱を空にする。
            //   remove() を呼ぶことで、このスレッドが次のリクエストに使い回されても
            //   前のテナントIDが残らない（別会社のデータ混線・メモリリークを防ぐ）。
            TenantContext.clear();
        }
    }
}
