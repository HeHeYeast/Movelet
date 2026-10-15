param([Parameter(Mandatory=$true)][string]$Url)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$uri = [uri]$Url
if ($uri.Host -ne 'repo.maven.apache.org' -and $uri.Host -ne 'dl.google.com') { throw 'Only original dependency repositories accepted' }
$path = $uri.AbsolutePath -replace '^/maven2/','' -replace '^/dl/android/maven2/',''
$parts = $path.Split('/')
$filename = $parts[-1]
$version = $parts[-2]
$artifact = $parts[-3]
$group = ($parts[0..($parts.Length-4)] -join '.')
$temp = Join-Path $root '.toolchain/downloads'
New-Item -ItemType Directory -Force $temp | Out-Null
& 'C:\Windows\System32\curl.exe' -fsSL --retry 3 --max-time 180 -o "$temp/$filename" $Url
if ($LASTEXITCODE -ne 0) { throw 'Artifact download failed' }
& 'C:\Windows\System32\curl.exe' -fsSL --retry 3 --max-time 30 -o "$temp/$filename.sha1" "$Url.sha1"
if ($LASTEXITCODE -ne 0) { throw 'Checksum download failed' }
$hash = (Get-FileHash "$temp/$filename" -Algorithm SHA1).Hash.ToLower()
if ($hash -ne (Get-Content "$temp/$filename.sha1").Trim()) { throw 'Artifact checksum mismatch' }
$target = Join-Path $root ".toolchain/gradle-home/caches/modules-2/files-2.1/$group/$artifact/$version/$hash"
New-Item -ItemType Directory -Force $target | Out-Null
Copy-Item "$temp/$filename" $target
Write-Output "Verified official artifact: $group/$artifact/$version"
