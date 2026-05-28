$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$WorkspaceRoot = Split-Path $ProjectRoot -Parent
$RuntimeRoot = Join-Path $WorkspaceRoot "SmartStudyPlannerPomodoro\.android-runtime"
$env:ANDROID_HOME = Join-Path $RuntimeRoot "android-sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:ANDROID_HOME\platform-tools;$env:Path"

$Adb = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
$BuildFile = Join-Path $ProjectRoot "app\build.gradle.kts"
$BuildText = Get-Content -Raw $BuildFile
$VersionName = [regex]::Match($BuildText, 'versionName\s*=\s*"([^"]+)"').Groups[1].Value
$Apk = Join-Path $ProjectRoot "release\whatsapp\Silent-Scheduler-v$VersionName.apk"

if (-not (Test-Path $Apk)) {
    Write-Host "APK not found. First run:"
    Write-Host "  powershell -ExecutionPolicy Bypass -File .\scripts\prepare-distribution.ps1"
    exit 1
}

$Devices = & $Adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" }
if (-not $Devices) {
    Write-Host "No Android device is connected. Connect and unlock the phone, then accept USB debugging."
    exit 1
}

& $Adb install -r $Apk
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Install failed. If Android says signatures do not match, the phone has an older development build."
    Write-Host "Uninstall Silent Scheduler from that phone once, then run this script again."
    Write-Host "This script does not uninstall automatically because uninstalling removes local app data."
    exit $LASTEXITCODE
}

& $Adb shell monkey -p com.mhamz.prayerdndmanager -c android.intent.category.LAUNCHER 1
