package com.example.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

// リポジトリ = 「DBへの読み書きを担当する係」。
//   JpaRepository<Application, Long> を継承するだけで、save() や count() など
//   基本的なDB操作を Spring が自動で用意してくれる（自分でSQLを書かなくてよい）。
//   <Application, Long> = 「Application エンティティを、Long型の主キーで扱う」という指定。
public interface CampaignRepository extends JpaRepository<Application, Long> {

    // メソッドはこれ以上追加しなくてよい。
    //   ・保存      → 継承した save(entity) をそのまま使う
    //   ・合計応募数 → 継承した count() をそのまま使う（DBに数えさせるので常に正確）
    //
    // ★ポイント★
    //   bad版にあった「応募済みか？」をわざわざ調べるメソッドも、ここでは持たない。
    //   “事前に確認してから保存”ではなく、“とにかく保存を試して、DBのユニーク制約に
    //   弾かれたら「もう応募済みだった」と気づく” 方式にするから（Serviceを参照）。
}
