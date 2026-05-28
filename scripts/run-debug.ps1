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
$Apk = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"

Push-Location $ProjectRoot
try {
    & $Gradle :app:assembleDebug
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE."
    }

    $Devices = & $Adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" }
    if (-not $Devices) {
        Write-Host "No Android device or emulator is connected."
        Write-Host "Open an emulator or connect a phone with USB debugging, then run this script again."
        exit 1
    }

    & $Adb install -r $Apk
    if ($LASTEXITCODE -ne 0) {
        throw "APK install failed with exit code $LASTEXITCODE."
    }
    & $Adb shell monkey -p com.mhamz.prayerdndmanager -c android.intent.category.LAUNCHER 1
    if ($LASTEXITCODE -ne 0) {
        throw "App launch failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
