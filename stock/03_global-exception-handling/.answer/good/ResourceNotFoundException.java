package com.example.shop.api;

/**
 * 「探しているデータ（商品・注文など）が存在しなかった」ことを表す独自例外。
 *
 * ポイント：
 * ・「見つからない」という"状況"に専用の名前を付けることで、
 *   受け止める側（例外ハンドラ）が「これは404にすべきだ」と迷わず判断できる。
 * ・RuntimeException を継承 = 「呼び出し元に必ず try-catch を強制しない」例外。
 *   （Spring では try-catch せず投げっぱなしにし、ハンドラで一括処理するのが定石）
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message); // 親クラスにメッセージを渡すだけ
    }
}
