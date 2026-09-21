# reveal-answer.ps1 — 着手中の問題の「答え」を解禁する（Claude不要・ローカルのみ）
#   使い方: powershell -File scripts\reveal-answer.ps1 -Num 29
#   -Num は「本番番号」(着手時に採番された番号。ルート直下 RR_topic の RR)。
#   やること:
#     1) RR_*/.answer/good を RR_*/good へ出す
#     2) RR_*/.answer/ANSWER.md の中身を README.md の末尾へ追記
#     3) INDEX.md の該当行のNG欄（プレースホルダ）を本物のNG要約で置換
#     4) 封印フォルダ .answer を片付ける
#   ※ 必ず自分のレビューを書き終えてから実行すること（ネタバレ防止）。
param([Parameter(Mandatory=$true)][int]$Num)
$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
Set-Location $repo

$RR = '{0:D2}' -f $Num
$dir = Get-ChildItem -Directory $repo |
       Where-Object { $_.Name -like "${RR}_*" } | Select-Object -First 1
if (-not $dir) { Write-Error "ルートに ${RR}_* がありません。先に start-problem を実行してください"; exit 1 }

$ansDir = Join-Path $dir.FullName '.answer'
if (-not (Test-Path $ansDir)) { Write-Error "$($dir.Name)/.answer が見つかりません（解禁済み？）"; exit 1 }

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

# NG要約を先に読む（.answer 削除前に）
$ng = $null
$metaPath = Join-Path $ansDir 'meta.txt'
if (Test-Path $metaPath) {
    Get-Content -Encoding UTF8 $metaPath | ForEach-Object {
        if ($_ -match '^ng=(.*)$') { $ng = $Matches[1] }
    }
}

# git管理下かどうか（未コミットなら通常のファイル操作にフォールバック）
$oldEAP = $ErrorActionPreference; $ErrorActionPreference = 'SilentlyContinue'
& git ls-files --error-unmatch "$($dir.Name)/.answer/ANSWER.md" 2>&1 | Out-Null
$tracked = ($LASTEXITCODE -eq 0)
$ErrorActionPreference = $oldEAP

# 1) good/ をルート直下(問題フォルダ直下)に出す
if (Test-Path (Join-Path $ansDir 'good')) {
    if ($tracked) { git mv "$($dir.Name)/.answer/good" "$($dir.Name)/good" }
    else { Move-Item -LiteralPath (Join-Path $ansDir 'good') -Destination (Join-Path $dir.FullName 'good') }
}

# 2) ANSWER.md を README.md の末尾に追記
$answerMd = Join-Path $ansDir 'ANSWER.md'
$readme   = Join-Path $dir.FullName 'README.md'
if (Test-Path $answerMd) {
    $body = Get-Content -Raw -Encoding UTF8 $answerMd
    [System.IO.File]::AppendAllText($readme, "`r`n" + $body, $utf8NoBom)
    if ($tracked) { git rm -f "$($dir.Name)/.answer/ANSWER.md" | Out-Null }
}

# 3) INDEX.md のプレースホルダNGを本物に置換
if ($ng) {
    $indexPath = Join-Path $repo 'INDEX.md'
    $idx = Get-Content -Encoding UTF8 $indexPath
    for ($i = 0; $i -lt $idx.Count; $i++) {
        if ($idx[$i] -match "^\|\s*$RR\s*\|" -and $idx[$i] -match '（着手中：解禁後に記載）') {
            $idx[$i] = $idx[$i] -replace '（着手中：解禁後に記載）', $ng
            break
        }
    }
    [System.IO.File]::WriteAllText($indexPath, ($idx -join "`r`n") + "`r`n", $utf8NoBom)
}

# 4) 空になった .answer を片付ける
if (Test-Path $ansDir) { Remove-Item -Recurse -Force $ansDir -ErrorAction SilentlyContinue }

Write-Host "✅ 解禁: 第${Num}問（$($dir.Name)）の good/ と NG解説(README末尾) を公開しました。" -ForegroundColor Green
Write-Host "  INDEX.md のNG欄も更新しました。git status で差分を確認してください。"
