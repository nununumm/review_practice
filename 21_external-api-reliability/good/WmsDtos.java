package com.example.stock;

import java.util.List;

/**
 * WMS（外部倉庫システム）とやり取りする「一括問い合わせ用」のデータ入れ物たち。
 *
 * ポイント：商品ごとに1回ずつ叩くのをやめ、全商品を1つのリクエストにまとめて
 * 1回で問い合わせる（＝外部API版のN+1回避）。そのためのリクエスト/レスポンスの形。
 *
 * これらは外部連携専用の内部DTOなので public にせず、このパッケージ内だけで使う
 * （package-private）。record なので getter などは自動生成される。
 *
 * ※ 値を URL の文字列連結ではなく JSON の本文(body)に載せて送るため、特殊文字の
 *   エスケープは JSON 変換ライブラリが自動でやってくれる。bad版のような
 *   「?code=... の手組み」で壊れる問題も起きない。
 */
final class WmsDtos {
    private WmsDtos() {} // インスタンス化させないためのからのコンストラクタ
}

/** WMSへ送る一括リクエスト（問い合わせたい商品コードの一覧） */
record WmsBulkRequest(List<WmsItem> items) {
}

/** リクエスト内の1商品ぶん */
record WmsItem(String code) {
}

/** WMSからの一括レスポンス（各商品の在庫数の一覧） */
record WmsBulkResponse(List<WmsStockEntry> entries) {
}

/** レスポンス内の1商品ぶん（その商品の引き当て可能在庫数） */
record WmsStockEntry(String code, int availableQuantity) {
}
