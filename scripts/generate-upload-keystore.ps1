$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$WorkspaceRoot = Split-Path $ProjectRoot -Parent
$RuntimeRoot = Join-Path $WorkspaceRoot "SmartStudyPlannerPomodoro\.android-runtime"
$env:JAVA_HOME = Join-Path $RuntimeRoot "jdk17\jdk-17.0.19+10"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$Keytool = Join-Path $env:JAVA_HOME "bin\keytool.exe"
$KeystoreDir = Join-Path $ProjectRoot ".keystore"
$KeystorePath = Join-Path $KeystoreDir "prayer-dnd-manager-upload.jks"
$PropertiesPath = Join-Path $ProjectRoot "keystore.properties"
$Alias = "prayer-dnd-manager"

function New-StrongPassword {
    $bytes = New-Object byte[] 36
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $rng.GetBytes($bytes)
    } finally {
        $rng.Dispose()
    }
    return [Convert]::ToBase64String($bytes).TrimEnd("=") -replace "\+", "-" -replace "/", "_"
}

if ((Test-Path $KeystorePath) -and (Test-Path $PropertiesPath)) {
    Write-Host "Upload keystore and keystore.properties already exist."
    Write-Host "Keystore: $KeystorePath"
    exit 0
}

New-Item -ItemType Directory -Force -Path $KeystoreDir | Out-Null
$Password = New-StrongPassword

if (-not (Test-Path $KeystorePath)) {
    & $Keytool -genkeypair `
        -v `
        -keystore $KeystorePath `
        -storepass $Password `
        -keypass $Password `
        -alias $Alias `
        -keyalg RSA `
        -keysize 4096 `
        -validity 10000 `
        -dname "CN=Silent Scheduler, OU=Mobile Apps, O=Silent Scheduler, L=Karachi, ST=Sindh, C=PK" `
        -noprompt

    if ($LASTEXITCODE -ne 0) {
        throw "keytool failed with exit code $LASTEXITCODE."
    }
}

@"
storeFile=.keystore/prayer-dnd-manager-upload.jks
storePassword=$Password
keyAlias=$Alias
keyPassword=$Password
"@ | Set-Content -Path $PropertiesPath -Encoding ASCII -NoNewline

Write-Host "Created Play upload keystore and keystore.properties."
Write-Host "Keystore: $KeystorePath"
Write-Host "IMPORTANT: Back up the .keystore folder and keystore.properties privately. You need them for future Play Store updates."
