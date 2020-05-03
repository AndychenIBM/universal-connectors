#Save arguments
$protocol=$($args[0])
$address=$($args[1])

Write-Host "$(Get-Date): rsyslog params:`n`tPROTOCOL=$($protocol)`n`tADDRESS=$($address)`n"
