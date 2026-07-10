// Repository = 「DB（テーブル）とやり取りする担当」のクラス。
// JpaRepository<Stock, Long> を継承すると、findById や save など基本のDB操作が
// 自分で書かなくても最初から使えるようになる（<Stock=扱うテーブル, Long=主キーの型>）。
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * 在庫を「原子的（アトミック）」に減らすための自作メソッド。
     * 「原子的」＝「途中で割り込まれない、分割できない1つの操作」という意味。
     *
     * ポイントは WHERE 句の「s.quantity >= :count」。
     * 「在庫が注文数以上あるときだけ減らす」という条件をSQL自身に持たせることで、
     *   ・在庫不足のときは1行も更新されない（＝戻り値0）
     *   ・同時に2件の注文が来ても、DBが順番に処理するので在庫がマイナスにならない
     * という2つを、たった1文のSQLで同時に実現できる。
     *
     * @return 更新できた行数（1 = 減算成功 / 0 = 在庫不足で減らせなかった）
     */
    // @Modifying = このクエリは「データを書き換える(UPDATE/DELETE)」ものだ、という宣言。
    //              （付けないと「読み取り専用」とみなされてエラーになる）
    @Modifying
    // @Query = 実行したいSQL（正確にはJPQL）を自分で指定する。
    //          :productId, :count は下の引数から値が差し込まれる場所（プレースホルダ）。
    @Query("UPDATE Stock s SET s.quantity = s.quantity - :count " +
           "WHERE s.productId = :productId AND s.quantity >= :count")
    // @Param("...") = メソッドの引数を、上のSQLの :名前 に結びつける指定。
    int decreaseStock(@Param("productId") Long productId, @Param("count") int count);
}
