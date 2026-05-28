$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$WorkspaceRoot = Split-Path $ProjectRoot -Parent
$RuntimeRoot = Join-Path $WorkspaceRoot "SmartStudyPlannerPomodoro\.android-runtime"
$PropertiesPath = Join-Path $ProjectRoot "keystore.properties"

if (-not (Test-Path $PropertiesPath)) {
    Write-Host "Play Store builds must use your private upload key."
    Write-Host "First run:"
    Write-Host "  powershell -ExecutionPolicy Bypass -File .\scripts\generate-upload-keystore.ps1"
    Write-Host "Then copy keystore.properties.example to keystore.properties and fill in your passwords."
    exit 1
}

$env:JAVA_HOME = Join-Path $RuntimeRoot "jdk17\jdk-17.0.19+10"
$env:ANDROID_HOME = Join-Path $RuntimeRoot "android-sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
$Gradle = Join-Path $RuntimeRoot "gradle\gradle-8.9\bin\gradle.bat"

Push-Location $ProjectRoot
try {
    & $Gradle :app:bundleRelease
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle Play Store bundle build failed with exit code $LASTEXITCODE."
    }
    Write-Host ""
    Write-Host "Play Store AAB:"
    Write-Host (Join-Path $ProjectRoot "app\build\outputs\bundle\release\app-release.aab")
} finally {
    Pop-Location
}
