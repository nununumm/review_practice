# archive-done.ps1 —（任意）解答済みの問題を archive/ に片付ける
#   使い方: powershell -File scripts\archive-done.ps1 -Num 29
#   注意 : フォルダを移動するとパスが変わります（REVIEW_LOG.md 等のリンクに影響）。
#          普段は STOCK.md / INDEX.md の「状態」列で完了管理する方が安全です。
#          見た目が散らかってきたら、まとめて片付ける用途にだけ使ってください。
param([Parameter(Mandatory=$true)][int]$Num)
$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
Set-Location $repo

$nn = '{0:D2}' -f $Num
$dir = Get-ChildItem -Directory $repo |
       Where-Object { $_.Name -like "${nn}_*" } | Select-Object -First 1
if (-not $dir) { Write-Error "ルートに ${nn}_* がありません"; exit 1 }

if (Test-Path (Join-Path $dir.FullName '.answer')) {
    Write-Error "$($dir.Name) はまだ答えが封印されています。先に reveal-answer で解禁・レビュー完了してください"; exit 1
}
if (-not (Test-Path (Join-Path $dir.FullName 'good'))) {
    Write-Warning "$($dir.Name) に good/ がありません。本当に解答済みですか？"
}

New-Item -ItemType Directory -Force (Join-Path $repo 'archive') | Out-Null
git mv "$($dir.Name)" "archive/$($dir.Name)"
Write-Host "📦 archive/$($dir.Name) に移動しました。" -ForegroundColor Green
Write-Host "  ※ INDEX.md の場所欄と、リンク切れが起きた REVIEW_LOG.md を更新してください。"
