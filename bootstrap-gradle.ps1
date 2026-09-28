$ErrorActionPreference = 'Stop'
$version = '8.12'

$userHome = [Environment]::GetFolderPath('UserProfile')
$tools = Join-Path $userHome '.gradle\planetary-tools'
$gradleHome = Join-Path $tools "gradle-$version"
$zip = Join-Path $tools "gradle-$version-bin.zip"

if (!(Test-Path $gradleHome)) {
  New-Item -ItemType Directory -Force -Path $tools | Out-Null
  Write-Host "Downloading Gradle $version once into $tools..."
  Invoke-WebRequest "https://services.gradle.org/distributions/gradle-$version-bin.zip" -OutFile $zip
  Expand-Archive -Path $zip -DestinationPath $tools -Force
  Remove-Item $zip -Force
} else {
  Write-Host "Using cached Gradle $version from $gradleHome"
}

& (Join-Path $gradleHome 'bin/gradle.bat') @args
exit $LASTEXITCODE
