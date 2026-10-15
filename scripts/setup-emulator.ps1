$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
$curl = 'C:\Windows\System32\curl.exe'
function DownloadVerified($url, $path, $sha1) {
    $size = if ($path -match 'system-image') { 782404023L } else { 455342868L }
    $segmentSize = [long][Math]::Ceiling($size / 8.0)
    $processes = @()
    $fullPath = Join-Path $root $path
    for ($i=0; $i -lt 8; $i++) {
        $start = $i * $segmentSize
        $end = [Math]::Min($size-1, $start+$segmentSize-1)
        $processes += Start-Process -FilePath $curl -ArgumentList @('-fsSL','--retry','3','--max-time','600','--range',"$start-$end",'-o',"$fullPath.part$i",$url) -WindowStyle Hidden -PassThru
    }
    foreach ($p in $processes) { $p.WaitForExit(); if ($p.ExitCode -ne 0) { throw 'Segment download failed' } }
    $output = [IO.File]::Create($fullPath)
    try {
        for ($i=0; $i -lt 8; $i++) {
            $part = [IO.File]::OpenRead("$fullPath.part$i")
            try { $part.CopyTo($output) } finally { $part.Dispose() }
        }
    } finally { $output.Dispose() }
    if ((Get-FileHash $path -Algorithm SHA1).Hash.ToLower() -ne $sha1) { throw 'Checksum mismatch' }
    Write-Output "Verified $path"
}
DownloadVerified 'https://dl.google.com/android/repository/emulator-windows_x64-16428233.zip' '.toolchain/emulator.zip' '488ed747e82de7e9bb5247becd1ac043c7e5e85d'
Expand-Archive '.toolchain/emulator.zip' '.toolchain/sdk' -Force
& '.toolchain/sdk/emulator/emulator.exe' -accel-check
DownloadVerified 'https://dl.google.com/android/repository/sys-img/android/x86_64-35_r02.zip' '.toolchain/system-image.zip' '2d857d170c0d1b827149565da34b3383e5306f7f'
Expand-Archive '.toolchain/system-image.zip' '.toolchain/sdk/system-images/android-35/default' -Force
Write-Output 'Emulator and system image verified and extracted.'
