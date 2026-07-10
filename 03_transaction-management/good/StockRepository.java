public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * 在庫を原子的（アトミック）に減算する。
     * WHERE 句に「s.quantity >= :count」を入れることで、在庫が足りるときだけ更新される。
     * - 在庫不足のときは更新件数 0 が返る（呼び出し側で例外にする）
     * - DBの1文で「読み取り＋条件判定＋書き込み」が完結するため、
     *   複数リクエストが同時に来ても在庫がマイナスにならない（ロストアップデート対策）。
     *
     * @return 更新された行数（1 = 成功, 0 = 在庫不足）
     */
    @Modifying
    @Query("UPDATE Stock s SET s.quantity = s.quantity - :count " +
           "WHERE s.productId = :productId AND s.quantity >= :count")
    int decreaseStock(@Param("productId") Long productId, @Param("count") int count);
}
