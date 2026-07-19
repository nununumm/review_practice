package com.example.shop.controller;

import com.example.shop.dto.CreateOrderRequest;
import com.example.shop.dto.OrderResponse;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

// @RestController = このクラスは「JSONを返すWeb API」の窓口だと宣言する印
@RestController
// @RequestMapping("/orders") = このクラスの全APIは「/orders」という“モノ”を扱う、と土台のURLを決める。
//   URLに /createOrder のような「動詞」を入れず、名詞（orders）だけにするのがRESTの作法。
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    // 依存はコンストラクタで受け取る（DI）。第8問で学んだ「newしない・外から渡す」の実践。
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 【一覧取得】GET /orders?page=0&size=20
    // @GetMapping = 「見るだけ（GET）でしか呼べない」と限定する。安全な操作なのでGETが適切。
    // Pageable = Spring が用意する「何ページ目を何件」を受け取る仕組み。
    //   → 全件返さず“区切って”返すので、注文が100万件あってもサーバーが落ちない。
    @GetMapping
    public Page<OrderResponse> getOrders(Pageable pageable) {
        // Entity（DB直結クラス）ではなく、外部公開用のOrderResponse（DTO）に詰め替えて返す。
        // これで「見せたくない項目（原価・内部フラグ等）」がJSONに漏れない。
        return orderService.findOrders(pageable).map(OrderResponse::from);
    }

    // 【1件取得】GET /orders/1
    // {id} = URLの一部としてIDを受け取る書き方（?id=1 ではなく /orders/1 とするのがRESTらしい）。
    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        // 見つからなければ Service 側で例外を投げ、@ExceptionHandler が 404 に変換する（下部参照）。
        // 「空のOrderを200で返す」ような“嘘の成功”はしない。
        return OrderResponse.from(orderService.getById(id));
    }

    // 【作成】POST /orders
    // @PostMapping = 「作る（POST）」でしか呼べない。GETで作成できてしまう事故を防ぐ。
    // @Valid @RequestBody = リクエストのJSON本文を受け取り、入力チェック（バリデーション）もかける。
    //   IDや数量を ?userId=1&quantity=2 のようにURLにぶら下げず、JSON本文で受けるのが素直。
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        // try/catch で握りつぶさない。業務エラーは例外として投げ、@ExceptionHandler が適切な4xxに変換。
        OrderResponse created = OrderResponse.from(
                orderService.create(request.userId(), request.productId(), request.quantity()));

        // 作成成功は 201 Created を返すのがRESTの作法。
        // さらに Location ヘッダで「作った注文はこのURLで取れますよ」と案内する。
        URI location = uriBuilder.path("/orders/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // 【削除】DELETE /orders/1
    // @DeleteMapping = 「消す（DELETE）」でしか呼べない。GETで削除できる危険な状態を根絶する。
    // @ResponseStatus(NO_CONTENT) = 削除成功は「204 No Content（成功したが返す中身はない）」。
    //   "deleted" のような文字列を返さず、ステータスコードで成否を伝える。
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        // 存在しないIDなら Service 側で例外 → 404 に変換される（存在チェックは Service に集約）。
        orderService.delete(id);
    }
}
