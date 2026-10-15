param([ValidateSet('read','tap','shot')][string]$Action='read',[string]$Text,[string]$Name)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
Set-Location $root
$adb=Join-Path $root '.toolchain/sdk/platform-tools/adb.exe'
function Read-Ui {
    & $adb -s emulator-5554 shell uiautomator dump /sdcard/movelet-ui.xml | Out-Null
    & $adb -s emulator-5554 pull /sdcard/movelet-ui.xml '.toolchain/movelet-ui.xml' | Out-Null
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
    & $adb -s emulator-5554 shell input tap $x $y
} else {
    if ($Name -notmatch '^[0-9a-z-]+$') { throw 'Use a simple screenshot name' }
    Read-Ui | Out-Null
    & $adb -s emulator-5554 shell screencap -p /sdcard/movelet-shot.png
    & $adb -s emulator-5554 pull /sdcard/movelet-shot.png "deliverables/screenshots/$Name.png"
}
