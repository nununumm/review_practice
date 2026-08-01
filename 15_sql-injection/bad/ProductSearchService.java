package com.example.shop.product;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
public class ProductSearchService {

    private final DataSource dataSource;

    public ProductSearchService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 商品名でのあいまい検索。カテゴリと並び順も画面から指定できる。
    public List<Product> search(String keyword, String category, String sortColumn) {
        List<Product> results = new ArrayList<>();

        String sql = "SELECT id, name, price, category FROM products WHERE 1=1";
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND name LIKE '%" + keyword + "%'";
        }
        if (category != null && !category.isEmpty()) {
            sql += " AND category = '" + category + "'";
        }
        sql += " ORDER BY " + sortColumn;

        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getInt("price"));
                p.setCategory(rs.getString("category"));
                results.add(p);
            }
        } catch (Exception e) {
            System.out.println("検索でエラー: " + e.getMessage());
        }

        return results;
    }
}
