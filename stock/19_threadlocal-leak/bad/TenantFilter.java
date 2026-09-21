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
 * リクエストの入口で、ヘッダーから受け取ったテナントIDを TenantContext に入れる。
 * これ以降、同じリクエストの処理中はどこからでもテナントIDを参照できる。
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

        // 後続の処理（コントローラやサービス）へ処理を渡す
        chain.doFilter(request, response);

        // 処理が終わったので保管箱を空にする
        TenantContext.setTenantId(null);
    }
}
