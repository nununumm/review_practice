package com.example.shop.product;

/**
 * 「商品が見つからなかった」ことを表す専用の例外クラス。
 *
 * ポイント：
 * ・ただの RuntimeException ではなく "名前を付けた箱" を用意することで、
 *   後述の GlobalExceptionHandler が「これは404にすべきエラーだ」と種類で見分けられる。
 * ・中身は「親(RuntimeException)にメッセージを渡すだけ」の非常に小さいクラスでよい。
 */
public class ProductNotFoundException extends RuntimeException {

    // id を受け取って、人間に分かるメッセージを組み立てて親に渡すだけ
    public ProductNotFoundException(Long id) {
        super("商品が見つかりません。id=" + id);
    }
}
