# start-problem.ps1 — 未着手プール(stock/)の問題を1つ「着手中」にする
#   使い方: powershell -File scripts\start-problem.ps1 -Num 1
#   -Num は「ストックの並び番号」(stock/01_ , 02_ ...)。
#   やること:
#     1) 既存問題の「最大番号+1」を本番番号として採番する（着手した瞬間に確定）
#     2) stock/NN_topic をリポジトリ直下へ本番番号付き(RR_topic)で移動
#     3) README の見出しを「# 第R問：…」に書き換える
#     4) INDEX.md の既出テーマ一覧に1行追記（NG欄は伏せてプレースホルダ）
#   ※ 答え(.answer/)は封印されたまま同梱される（開かないこと）。
param([Parameter(Mandatory=$true)][int]$Num)
$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
Set-Location $repo

$nn = '{0:D2}' -f $Num
$src = Get-ChildItem -Directory (Join-Path $repo 'stock') -ErrorAction SilentlyContinue |
       Where-Object { $_.Name -like "${nn}_*" } | Select-Object -First 1
if (-not $src) { Write-Error "stock/${nn}_* が見つかりません（並び番号を確認: STOCK.md）"; exit 1 }

# --- 1) 本番番号を採番（ルート直下の NN_ フォルダの最大値+1）---
$nums = Get-ChildItem -Directory $repo |
        Where-Object { $_.Name -match '^\d{2}_' } |
        ForEach-Object { [int]($_.Name.Substring(0,2)) }
$max  = [int](($nums | Measure-Object -Maximum).Maximum)  # .Maximum は Double で返るため int 化
$next = $max + 1
$RR   = '{0:D2}' -f $next

$topic   = $src.Name.Substring(3)          # "NN_" の後ろ
$destName = "${RR}_${topic}"
$dest = Join-Path $repo $destName
if (Test-Path $dest) { Write-Error "$destName は既に存在します"; exit 1 }

# --- 2) 移動（git管理下なら git mv、未追跡なら通常移動）---
$oldEAP = $ErrorActionPreference; $ErrorActionPreference = 'SilentlyContinue'
& git ls-files --error-unmatch "stock/$($src.Name)" 2>&1 | Out-Null
$tracked = ($LASTEXITCODE -eq 0)
$ErrorActionPreference = $oldEAP
if ($tracked) { git mv "stock/$($src.Name)" "$destName" }
else { Move-Item -LiteralPath (Join-Path $repo "stock\$($src.Name)") -Destination $dest }

# --- 3) README 見出しを本番番号に ---
$readme = Join-Path $dest 'README.md'
$lines  = Get-Content -Encoding UTF8 $readme
$title  = ($lines[0] -replace '^#\s*【ストック】', '' -replace '^#\s*第\d+問：', '' -replace '^#\s*', '')
$lines[0] = "# 第${next}問：${title}"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($readme, ($lines -join "`r`n") + "`r`n", $utf8NoBom)

# --- 4) INDEX.md に1行追記（メタから。NGは解禁後に埋める）---
$meta = @{}
Get-Content -Encoding UTF8 (Join-Path $dest '.answer\meta.txt') | ForEach-Object {
    if ($_ -match '^(.*?)=(.*)$') { $meta[$Matches[1]] = $Matches[2] }
}
$theme = $meta['index_theme']; $persp = $meta['perspective']
$row = "| $RR | $theme | $persp | （着手中：解禁後に記載） |"
[System.IO.File]::AppendAllText((Join-Path $repo 'INDEX.md'), "`r`n$row", $utf8NoBom)

Write-Host "▶ 着手: 第${next}問 として $destName をルートに移動しました。" -ForegroundColor Green
Write-Host "  見えるのは bad/ と README(問題文) だけです。まず自分でレビューを書いてください。"
Write-Host "  レビューを書き終えたら:  powershell -File scripts\reveal-answer.ps1 -Num $next"
Write-Host "  （STOCK.md の状態を『着手中』、本番番号を $next に更新しておくと分かりやすいです）"
