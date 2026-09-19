#!/usr/bin/env bash
# =============================================================================
# mr.sh — コードレビュー特訓リポジトリ用の MR（マージ）自動化スクリプト
#
# ブランチ運用: feature(claude/...) -> develop -> master を段階的に --no-ff マージ。
# マージ後は feature ブランチをローカル・リモートとも削除し、develop に戻る。
#
# 使い方:
#   bash mr.sh finish "<branch>" "<commit-msg>" "<label>"
#       いまの作業を一気に MR する（通常はこれ1回だけ呼べばよい）。
#       1) <branch> に居なければ作成/切替（未コミットの変更は持ち越される）
#       2) 変更があれば commit
#       3) feature を push
#       4) feature -> develop を --no-ff マージして push
#       5) develop -> master を --no-ff マージして push
#       6) develop に戻る
#       7) feature ブランチをローカル・リモートとも削除
#
#   bash mr.sh start "<branch>"
#       新しい問題の作業を始めるときに、develop から feature を切って push するだけ。
#       （出題フェーズの頭で使う。finish だけでも自動作成するので必須ではない）
#
# 引数の例:
#   branch     = claude/20260919-q26-null-safety
#   commit-msg = 第26問（在庫引当のnull安全性）を出題
#   label      = Q26 (null-safety)      ← マージコミットの見出しに使う短いラベル
# =============================================================================
set -euo pipefail

DEVELOP="develop"
MASTER="master"

die() { echo "ERROR: $*" >&2; exit 1; }

cmd="${1:-}"
[ -n "$cmd" ] || die "サブコマンドを指定してください（start | finish）"

# --- 現在ブランチを取得（detached でないこと前提） ---
current_branch() { git symbolic-ref --short HEAD; }

# --- feature ブランチに移動（無ければ作る。未コミット変更は持ち越し） ---
ensure_on_branch() {
  local branch="$1"
  local cur
  cur="$(current_branch)"
  if [ "$cur" = "$branch" ]; then
    return
  fi
  if git show-ref --verify --quiet "refs/heads/$branch"; then
    git switch "$branch"
  elif git ls-remote --exit-code --heads origin "$branch" >/dev/null 2>&1; then
    git switch -c "$branch" --track "origin/$branch"
  else
    git switch -c "$branch"
  fi
}

case "$cmd" in
  start)
    branch="${2:-}"
    [ -n "$branch" ] || die "ブランチ名を指定してください: bash mr.sh start \"<branch>\""
    git switch "$DEVELOP"
    git pull --ff-only origin "$DEVELOP"
    ensure_on_branch "$branch"
    git push -u origin "$branch"
    echo "OK: '$branch' を develop から作成し push しました。ここで作業してください。"
    ;;

  finish)
    branch="${2:-}"
    commit_msg="${3:-}"
    label="${4:-}"
    [ -n "$branch" ]     || die "ブランチ名を指定してください（第1引数）"
    [ -n "$commit_msg" ] || die "コミットメッセージを指定してください（第2引数）"
    [ -n "$label" ]      || die "マージ用ラベルを指定してください（第3引数）例: 'Q26 (null-safety)'"

    # 1) feature ブランチへ（未コミットの変更は持ち越される）
    ensure_on_branch "$branch"

    # 2) 変更があれば commit（無ければスキップ）
    git add -A
    if git diff --cached --quiet; then
      echo "INFO: コミットする変更はありませんでした（新規コミットなしで進めます）。"
    else
      git commit -m "$commit_msg"
    fi

    # 3) feature を push
    git push -u origin "$branch"

    # 4) feature -> develop
    git switch "$DEVELOP"
    git pull --ff-only origin "$DEVELOP"
    git merge --no-ff "$branch" -m "Merge $label into develop"
    git push origin "$DEVELOP"

    # 5) develop -> master
    git switch "$MASTER"
    git pull --ff-only origin "$MASTER"
    git merge --no-ff "$DEVELOP" -m "Merge develop into master ($label)"
    git push origin "$MASTER"

    # 6) develop に戻る
    git switch "$DEVELOP"

    # 7) feature ブランチを削除（ローカル・リモート）
    git branch -D "$branch" || true
    git push origin --delete "$branch" || true

    echo "OK: '$branch' を develop -> master までマージ・push し、ブランチを削除しました。"
    echo "   マージラベル: $label"
    echo "   現在のブランチ: $(current_branch)"
    ;;

  *)
    die "未知のサブコマンド: '$cmd'（start | finish のいずれか）"
    ;;
esac
