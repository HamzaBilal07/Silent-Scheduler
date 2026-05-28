$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$WorkspaceRoot = Split-Path $ProjectRoot -Parent
$RuntimeRoot = Join-Path $WorkspaceRoot "SmartStudyPlannerPomodoro\.android-runtime"
$env:JAVA_HOME = Join-Path $RuntimeRoot "jdk17\jdk-17.0.19+10"
$env:ANDROID_HOME = Join-Path $RuntimeRoot "android-sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"

$Gradle = Join-Path $RuntimeRoot "gradle\gradle-8.9\bin\gradle.bat"
$Adb = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
$Apk = Join-Path $ProjectRoot "app\build\outputs\apk\release\app-release.apk"

Push-Location $ProjectRoot
try {
    & $Gradle :app:assembleRelease
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle release build failed with exit code $LASTEXITCODE."
    }

    $Devices = & $Adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" }
    if (-not $Devices) {
        Write-Host "No Android device or emulator is connected."
        Write-Host "Connect your phone, keep it unlocked, and accept the USB debugging prompt."
        exit 1
    }

    & $Adb install -r $Apk
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "APK install was blocked by the phone."
        Write-Host "Keep the phone unlocked and accept the install prompt."
        Write-Host "Enable Developer options > USB debugging."
        Write-Host "On Xiaomi/Redmi/POCO phones, also enable Install via USB and USB debugging (Security settings)."
        Write-Host "If Android says the app is incompatible, uninstall the old Silent Scheduler once, then run this script again."
        throw "APK install failed with exit code $LASTEXITCODE."
    }
    & $Adb shell monkey -p com.mhamz.prayerdndmanager -c android.intent.category.LAUNCHER 1
    if ($LASTEXITCODE -ne 0) {
        throw "App launch failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
