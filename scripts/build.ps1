param([string[]]$Tasks = @('testDebugUnitTest', 'lintDebug', 'assembleDebug'))
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location $projectRoot
if (Test-Path "$projectRoot/.toolchain/java") { $env:JAVA_HOME = (Get-ChildItem "$projectRoot/.toolchain/java" -Directory | Select-Object -First 1).FullName }
if (-not $env:JAVA_HOME) { throw 'Configure JAVA_HOME with a full JDK 17, including jlink.' }
$env:ANDROID_HOME = Join-Path $projectRoot '.toolchain/sdk'
$env:ANDROID_USER_HOME = Join-Path $projectRoot '.toolchain/android-user'
$env:GRADLE_USER_HOME = Join-Path $projectRoot '.toolchain/gradle-home'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& "$projectRoot/.toolchain/gradle-8.11.1/bin/gradle.bat" @Tasks --console=plain
exit $LASTEXITCODE
