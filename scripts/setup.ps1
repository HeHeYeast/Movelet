$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$cache = Join-Path $root '.toolchain'
New-Item -ItemType Directory -Force $cache | Out-Null
$curl = 'C:\Windows\System32\curl.exe'
function Fetch($url, $path) {
    & $curl -fL --retry 3 --connect-timeout 30 --output $path $url
    if ($LASTEXITCODE -ne 0) { throw "Download failed: $url" }
}
Fetch 'https://dl.google.com/android/repository/repository2-1.xml' "$cache/repository.xml"
[xml]$repo = Get-Content "$cache/repository.xml"
$package = $repo.SelectNodes('//*[local-name()="remotePackage"]') | Where-Object { $_.path -eq 'cmdline-tools;19.0' }
$archive = $package.archives.archive | Where-Object { $_.'host-os' -eq 'windows' }
if (-not $archive) { throw 'Official command line tools entry unavailable' }
if (-not (Test-Path "$cache/commandline.zip")) { Fetch ('https://dl.google.com/android/repository/' + $archive.complete.url) "$cache/commandline.zip" }
$actual = (Get-FileHash "$cache/commandline.zip" -Algorithm SHA1).Hash.ToLower()
if ($actual -ne [string]$archive.complete.checksum) { throw 'SDK checksum mismatch' }
New-Item -ItemType Directory -Force "$cache/sdk/cmdline-tools" | Out-Null
if (-not (Test-Path "$cache/sdk/cmdline-tools/latest")) {
    Expand-Archive "$cache/commandline.zip" "$cache/unpack" -Force
    Move-Item -LiteralPath "$cache/unpack/cmdline-tools" -Destination "$cache/sdk/cmdline-tools/latest"
}
Fetch 'https://downloads.gradle.org/distributions/gradle-8.11.1-bin.zip' "$cache/gradle.zip"
Fetch 'https://downloads.gradle.org/distributions/gradle-8.11.1-bin.zip.sha256' "$cache/gradle.sha256"
if ((Get-FileHash "$cache/gradle.zip" -Algorithm SHA256).Hash.ToLower() -ne (Get-Content "$cache/gradle.sha256").Trim()) { throw 'Gradle checksum mismatch' }
Expand-Archive "$cache/gradle.zip" $cache -Force
Write-Output 'Official SDK tools and Gradle downloaded and checksums verified.'
