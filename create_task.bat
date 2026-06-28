@echo off
chcp 65001 > nul
setlocal

:INPUT_LOOP
echo --------------------------------------------------
:: ユーザーに入力を求める（前後の余計なスペースをリセット）
set "DIR_NAME="
set /p DIR_NAME="作成するフォルダ名を入力してください（例: 03_xxx）: "

:: 入力チェック（ダブルクォーテーションで囲んで空白を厳密に判定）
if "%DIR_NAME%"=="" (
    echo.
    echo 【エラー】フォルダ名が空っぽです。もう一度入力してください。
    echo.
    goto INPUT_LOOP
)

:: 1. フォルダの作成
mkdir "%DIR_NAME%\bad"
mkdir "%DIR_NAME%\good"

:: 2. README.mdの作成
echo # 課題タイトルをここに書く> "%DIR_NAME%\README.md"
echo.>> "%DIR_NAME%\README.md"

:: 3. Javaファイル（空）の作成（※必要に応じてファイル名は後で変えてね）
type nul > "%DIR_NAME%\bad\BadExample.java"
type nul > "%DIR_NAME%\good\GoodExample.java"

echo.
echo [%DIR_NAME%] のフォルダとファイルの生成が完了しました！
pause