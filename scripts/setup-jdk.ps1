$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
$manifest = Get-Content '.toolchain/jdk-manifest.json' -Raw | ConvertFrom-Json
$package = $manifest[0].binary.package
$curl = 'C:\Windows\System32\curl.exe'
$segmentSize = [long][Math]::Ceiling($package.size / 4.0)
$jobs = @()
for ($i=0; $i -lt 4; $i++) {
    $start=$i*$segmentSize; $end=[Math]::Min($package.size-1,$start+$segmentSize-1)
    if ((Test-Path "$root/.toolchain/jdk.zip.part$i") -and (Get-Item "$root/.toolchain/jdk.zip.part$i").Length -eq ($end-$start+1)) { continue }
    $jobs += Start-Process $curl -ArgumentList @('-fsSL','--retry','4','--retry-all-errors','--max-time','600','--range',"$start-$end",'-o',"$root/.toolchain/jdk.zip.part$i",$package.link) -WindowStyle Hidden -PassThru
}
foreach ($p in $jobs) { $p.WaitForExit(); if ($p.ExitCode -ne 0) { throw 'JDK download failed' } }
$output=[IO.File]::Create("$root/.toolchain/jdk.zip")
try { for ($i=0;$i -lt 4;$i++) { $part=[IO.File]::OpenRead("$root/.toolchain/jdk.zip.part$i"); try { $part.CopyTo($output) } finally { $part.Dispose() } } } finally { $output.Dispose() }
if ((Get-FileHash '.toolchain/jdk.zip' -Algorithm SHA256).Hash.ToLower() -ne $package.checksum) { throw 'JDK checksum mismatch' }
Expand-Archive '.toolchain/jdk.zip' '.toolchain/java' -Force
Write-Output 'Full JDK 17 downloaded, SHA256 verified and extracted.'
