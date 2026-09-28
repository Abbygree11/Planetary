$ErrorActionPreference = 'Stop'

$runDir = Join-Path $PSScriptRoot 'run'
$optionsFile = Join-Path $runDir 'options.txt'
New-Item -ItemType Directory -Force -Path $runDir | Out-Null

if (Test-Path $optionsFile) {
    $lines = Get-Content $optionsFile
    $found = $false
    $lines = $lines | ForEach-Object {
        if ($_ -match '^soundCategory_music:') {
            $found = $true
            'soundCategory_music:0.0'
        } else {
            $_
        }
    }
    if (-not $found) { $lines += 'soundCategory_music:0.0' }
    Set-Content -Path $optionsFile -Value $lines
} else {
    Set-Content -Path $optionsFile -Value 'soundCategory_music:0.0'
}

Write-Host 'Planetary dev client: Minecraft music is muted (other sounds unchanged).'
& "$PSScriptRoot/bootstrap-gradle.ps1" runClient --warning-mode=none --no-problems-report
exit $LASTEXITCODE
