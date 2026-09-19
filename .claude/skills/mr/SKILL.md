---
name: mr
description: このリポジトリのMR（マージ）作業を自動化する。ユーザーが「マージして」「MRして」「masterに上げて」等と言ったときに使う。feature(claude/...) を develop 経由で master まで段階的に --no-ff マージし、push して feature ブランチを削除する。新しい問題の作業開始時にブランチを切る用途にも使える。
---

# mr — MR（マージ）自動化スキル

このリポジトリのブランチ運用（`feature(claude/...) → develop → master` の段階マージ）を
1つの補助スクリプトで実行する。判断のブレとトークンを抑えるのが目的。

## いつ使うか

- ユーザーが **「マージして」「MRして」「masterに上げて」「反映して」** 等と言ったとき → `finish`
- 新しい問題の作業を **始めるとき**（出題フェーズの頭でブランチを切りたいとき） → `start`（任意）

## 使い方（通常はこれだけ）

作業内容が手元にある状態で、次の1コマンドを実行する。これで
「ブランチ作成/切替 → commit → feature push → develop マージ&push → master マージ&push → develop に戻る → feature ブランチ削除」
まで一気に走る。

```bash
bash .claude/skills/mr/scripts/mr.sh finish "<branch>" "<commit-msg>" "<label>"
```

### 引数の決め方

| 引数 | 内容 | 例 |
|------|------|-----|
| `<branch>` | `claude/YYYYMMDD-qNN-topic` 形式。日付は今日、NN は対象問題番号、topic はテーマ | `claude/20260919-q26-null-safety` |
| `<commit-msg>` | 実際の作業内容を表す日本語のコミットメッセージ（既存コミットの語調に合わせる） | `第26問（在庫引当のnull安全性）を出題` |
| `<label>` | マージコミットの見出しに入る短いラベル。`QNN (topic)` 形式が既存スタイル | `Q26 (null-safety)` |

`<label>` は次のマージコミットに使われる（既存履歴と同形式）:
- develop: `Merge <label> into develop`
- master: `Merge develop into master (<label>)`

### 同じ日・同じ問題で複数回 MR する場合

feature ブランチは毎回マージ後に削除される。同じ日・同じ問題で再度 MR したいときは、
**同じ `<branch>` 名でもう一度 `finish` を呼べばよい**（スクリプトが develop から切り直す）。
`<commit-msg>` はその回の作業内容に、`<label>` は `Q26 (null-safety, 2回目)` のように区別しても、同じままでもよい。

## 作業開始時にブランチだけ切りたいとき（任意）

```bash
bash .claude/skills/mr/scripts/mr.sh start "<branch>"
```

develop を最新化してから feature ブランチを切って push するだけ。以降はそのブランチで作業する。
（`finish` はブランチが無ければ自動で作るので、`start` を飛ばしても動く）

## 注意

- **ユーザーが「マージして」等と明示したときだけ実行する。** 勝手にコミット・マージしない（CLAUDE.md のGit運用ルール）。
- `master → develop` の順は飛ばさない（スクリプトが `develop` 経由を保証している）。
- 実行後はスクリプトが `OK:` 行を出す。それをユーザーに1〜2行で報告すれば十分（冗長なログ再掲は不要）。
- pull は `--ff-only`。もしリモートと分岐して失敗したら、無理に進めず状況をユーザーに報告する。
