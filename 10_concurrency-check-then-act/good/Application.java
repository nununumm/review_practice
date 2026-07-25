package com.example.campaign;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// @Entity = このクラスは「DBのテーブルの1行」に対応する入れ物ですよ、という印。
//   Application オブジェクト1個 = applications テーブルの1行、という関係になる。
@Entity
// @Table(...) で、このエンティティが対応するテーブル名と「制約（ルール）」を指定する。
//   uniqueConstraints = 「この列の組み合わせは重複させない」というDB側の“門番”。
//   ここでは user_id 列に「同じ値は2つ入れさせない」というユニーク制約をかけている。
//   ★これが今回の主役★ ── たとえ2つの応募が同時に保存されようとしても、
//     DB自身が2件目を必ず弾いてくれる。アプリのメモリに頼らず、DBが正しさを保証する。
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(name = "uk_applications_user_id", columnNames = "user_id")
)
public class Application {

    // @Id = この列が「主キー（＝1行を一意に識別する番号）」だという印。
    // @GeneratedValue = 番号はDBが自動で振ってくれる（自分でセットしなくてよい）。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(nullable = false) = この列は空（NULL）を許さない、というルール。
    //   name = "user_id" で、上のユニーク制約が見ている列名と一致させている。
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // JPA（DBとオブジェクトを橋渡しする仕組み）は、内部的に引数なしコンストラクタを必要とする。
    //   protected にしておくと「業務コードからは誤って使わせない」意図を示せる。
    protected Application() {
    }
    // 業務コードから応募を作るときはこちら。userId を必ず渡させることで“空の応募”を防ぐ。
    public Application(Long userId) {
        this.userId = userId;
    }

    
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }
}
