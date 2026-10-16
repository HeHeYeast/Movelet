param([ValidateSet('read','tap','shot')][string]$Action='read',[string]$Text,[string]$Name,
    [string]$Serial='emulator-5554',[string]$OutputDirectory='deliverables/personal/screenshots')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
Set-Location $root
$adb=Join-Path $root '.toolchain/sdk/platform-tools/adb.exe'
function Assert-MoveletForeground {
    $foreground = & $adb -s $Serial shell dumpsys activity activities
    if (-not ($foreground | Select-String '(mResumedActivity|topResumedActivity).*cn.movelet.app/.MainActivity')) { throw 'Movelet must be foreground before UI inspection' }
}
function Read-Ui {
    Assert-MoveletForeground
    & $adb -s $Serial shell uiautomator dump /sdcard/movelet-ui.xml | Out-Null
    & $adb -s $Serial pull /sdcard/movelet-ui.xml '.toolchain/movelet-ui.xml' | Out-Null
    [xml]$doc=Get-Content '.toolchain/movelet-ui.xml'
    return $doc
}
if ($Action -eq 'read') {
    $doc=Read-Ui
    $doc.SelectNodes('//node') | Where-Object { $_.text -or $_.'content-desc' } | Select-Object text,'content-desc',bounds
} elseif ($Action -eq 'tap') {
    $doc=Read-Ui
    $node=$doc.SelectNodes('//node') | Where-Object { $_.text -eq $Text -or $_.'content-desc' -eq $Text } | Select-Object -Last 1
    if (-not $node) { throw "Visible UI text not found: $Text" }
    $bounds=[regex]::Matches($node.bounds,'\d+') | ForEach-Object { [int]$_.Value }
    $x=[int](($bounds[0]+$bounds[2])/2); $y=[int](($bounds[1]+$bounds[3])/2)
    Assert-MoveletForeground
    & $adb -s $Serial shell input tap $x $y
} else {
    if ($Name -notmatch '^[0-9a-z-]+$') { throw 'Use a simple screenshot name' }
    Read-Ui | Out-Null
    Assert-MoveletForeground
    & $adb -s $Serial shell screencap -p /sdcard/movelet-shot.png
    Assert-MoveletForeground
    New-Item -ItemType Directory -Force $OutputDirectory | Out-Null
    & $adb -s $Serial pull /sdcard/movelet-shot.png (Join-Path $OutputDirectory "$Name.png")
}
