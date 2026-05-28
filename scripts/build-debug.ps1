$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$WorkspaceRoot = Split-Path $ProjectRoot -Parent
$RuntimeRoot = Join-Path $WorkspaceRoot "SmartStudyPlannerPomodoro\.android-runtime"
$env:JAVA_HOME = Join-Path $RuntimeRoot "jdk17\jdk-17.0.19+10"
$env:ANDROID_HOME = Join-Path $RuntimeRoot "android-sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
$Gradle = Join-Path $RuntimeRoot "gradle\gradle-8.9\bin\gradle.bat"

Push-Location $ProjectRoot
try {
    & $Gradle :app:assembleDebug
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
