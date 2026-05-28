$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$WorkspaceRoot = Split-Path $ProjectRoot -Parent
$RuntimeRoot = Join-Path $WorkspaceRoot "SmartStudyPlannerPomodoro\.android-runtime"
$PropertiesPath = Join-Path $ProjectRoot "keystore.properties"

if (-not (Test-Path $PropertiesPath)) {
    Write-Host "Release artifacts need a private upload key."
    Write-Host "Run:"
    Write-Host "  powershell -ExecutionPolicy Bypass -File .\scripts\generate-upload-keystore.ps1"
    exit 1
}

$env:JAVA_HOME = Join-Path $RuntimeRoot "jdk17\jdk-17.0.19+10"
$env:ANDROID_HOME = Join-Path $RuntimeRoot "android-sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
$Gradle = Join-Path $RuntimeRoot "gradle\gradle-8.9\bin\gradle.bat"

$BuildFile = Join-Path $ProjectRoot "app\build.gradle.kts"
$BuildText = Get-Content -Raw $BuildFile
$VersionName = [regex]::Match($BuildText, 'versionName\s*=\s*"([^"]+)"').Groups[1].Value
$VersionCode = [regex]::Match($BuildText, 'versionCode\s*=\s*(\d+)').Groups[1].Value
$ReleaseDir = Join-Path $ProjectRoot "release"
$ShareDir = Join-Path $ReleaseDir "whatsapp"

New-Item -ItemType Directory -Force -Path $ReleaseDir | Out-Null
New-Item -ItemType Directory -Force -Path $ShareDir | Out-Null

Push-Location $ProjectRoot
try {
    & $Gradle :app:assembleRelease :app:bundleRelease
    if ($LASTEXITCODE -ne 0) {
        throw "Release build failed with exit code $LASTEXITCODE."
    }

    $AabSource = Join-Path $ProjectRoot "app\build\outputs\bundle\release\app-release.aab"
    $ApkSource = Join-Path $ProjectRoot "app\build\outputs\apk\release\app-release.apk"
    $AabDest = Join-Path $ReleaseDir "Silent-Scheduler-v$VersionName-code$VersionCode-playstore.aab"
    $ApkDest = Join-Path $ShareDir "Silent-Scheduler-v$VersionName.apk"
    $ZipDest = Join-Path $ShareDir "Silent-Scheduler-v$VersionName-whatsapp.zip"

    Copy-Item -LiteralPath $AabSource -Destination $AabDest -Force
    Copy-Item -LiteralPath $ApkSource -Destination $ApkDest -Force
    if (Test-Path $ZipDest) {
        Remove-Item -LiteralPath $ZipDest -Force
    }
    Compress-Archive -LiteralPath $ApkDest -DestinationPath $ZipDest

    $HashFile = Join-Path $ReleaseDir "SHA256SUMS.txt"
    $HashLines = @($AabDest, $ApkDest, $ZipDest) | ForEach-Object {
        $RelativePath = Resolve-Path -LiteralPath $_ -Relative
        "$((Get-FileHash -Algorithm SHA256 -LiteralPath $_).Hash)  $RelativePath"
    }
    Set-Content -LiteralPath $HashFile -Value $HashLines -Encoding ASCII

    Write-Host "Play Store AAB:"
    Write-Host $AabDest
    Write-Host ""
    Write-Host "WhatsApp APK:"
    Write-Host $ApkDest
    Write-Host ""
    Write-Host "WhatsApp ZIP backup:"
    Write-Host $ZipDest
    Write-Host ""
    Write-Host "SHA-256 checksums:"
    Write-Host $HashFile
} finally {
    Pop-Location
}
