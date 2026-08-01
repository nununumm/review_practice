package com.example.report.good;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 注文テーブルにアクセスする係（リポジトリ）。
 *
 * ＜改善のキモ＞
 *  before では「全注文を取ってきて Java の for 文で合計」していた（データが増えるほど遅い）。
 *  ここでは SUM と COUNT を使い、"集計はDBにやらせて、返すのは数字だけ" にしている。
 *  → 注文が10万件ある日でも、10万個のオブジェクトをメモリに積まずに済む。
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 指定した日の「売上合計」と「注文件数」を、DB側で集計して1行だけ返す。
     *
     *  ・COALESCE(SUM(o.amount), 0) … その日の注文が0件でも SUM が null にならず 0 になるようにする保険。
     *  ・戻り値は DailySalesSummary（合計・件数だけを持つ入れ物）。
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) AS totalAmount, COUNT(o) AS orderCount "
            + "FROM Order o WHERE o.orderDate = :date")
    DailySalesSummary summarizeByOrderDate(@Param("date") LocalDate date);
}
