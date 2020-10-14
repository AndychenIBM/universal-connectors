$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Logfile = "$scriptDir\GUC.log"
$MONGODB_DEFAULT_CONF_PATH="C:\Program Files\MongoDB\Server\4.2\bin\mongod.cfg"

#Save arguments
$dest = $( $args[0] )
$format = $( $args[1] )
$path = $( $args[2] )
$filter = $( $args[3] )
$restart_mongodb = $( $args[4] )

Add-content $Logfile -value "$( Get-Date ): Params passed to mongod script:`n`tDEST=$( $dest )`n`tFORMAT=$( $format )`n`tPATH=$( $path )`n`tFILTER=$( $filter )`n`tRESTART_MONODB=$( $restart_mongodb )"
Write-Host "$( Get-Date ): Params passed to mongod script:`n`tDEST=$( $dest )`n`tFORMAT=$( $format )`n`tPATH=$( $path )`n`tFILTER=$( $filter )`n`tRESTART_MONODB=$( $restart_mongodb )"

$mongod_conf=Get-Variable -Name MONGODB_CONF_PATH -Scope Global -ErrorAction SilentlyContinue
if($mongod_conf -eq $null){
    $mongod_conf = $MONGODB_DEFAULT_CONF_PATH
}
Add-content $Logfile -value "$( Get-Date ): Mongod configuration file path is: $( $mongod_conf )"

if ($restart_mongodb -eq 1) {
    #Backup configuration file
    Copy-Item $mongod_conf -Destination "$($MONGODB_DEFAULT_CONF_PATH).backup"
    Add-content $Logfile -value "$( Get-Date ): Created $( $mongod_conf ).backup as a backup configuration file"
} else {
    #Edit example file
    Copy-Item $mongod_conf -Destination "$($MONGODB_DEFAULT_CONF_PATH).guardium_example"
    $mongod_conf = "$( $mongod_conf ).guardium_example"
    Add-content $Logfile -value "$( Get-Date ): Ceated $( $mongod_conf ).guardium_example as an example"
}

#Find if audit section exists and set in mongod.conf
#If audit section exists , update audit destination to filebeat and update filters, if it does not exist, add the audit section to mongod.conf"
if(Select-String -Path $mongod_conf -Pattern "^auditLog" -Quiet){
    $startRange = (Get-Content $mongod_conf | select-string "^auditLog").LineNumber
    $endRange = $startRange + 10
    ."$scriptDir\replaceInLinesRange.ps1" $mongod_conf "destination:.*" "destination: $( $dest )" $startRange $endRange
    ."$scriptDir\replaceInLinesRange.ps1" $mongod_conf "format:.*" "format: $( $format )" $startRange $endRange
    ."$scriptDir\replaceInLinesRange.ps1" $mongod_conf "path:.*" "path: $( $path )" $startRange $endRange
    if (-Not (Test-Path variable:filter) -or ($filter -eq $null)) {
        ."$scriptDir\replaceInLinesRange.ps1" $mongod_conf "filter:" "#filter:" $startRange $endRange
        Add-content $Logfile -value "$( Get-Date ): Filter field is empty. commenting out filter line from $( $mongod_conf )"
    } else {
        ."$scriptDir\replaceInLinesRange.ps1" $mongod_conf "filter:.*" "filter: $( $filter )" $startRange $endRange

    }
    ."$scriptDir\replaceInLinesRange.ps1" $mongod_conf "setParameter: {auditAuthorizationSuccess: true}" "setParameter: {auditAuthorizationSuccess: true}" $startRange $endRange
    Add-content $Logfile -value "$( Get-Date ): AuditLog was updated in $( $mongod_conf )"
} elseif(Select-String -Path $mongod_conf -Pattern "^#auditLog" -Quiet){
    ((Get-Content -path $mongod_conf) -replace '^#auditLog',"auditLog:`n  destination: $( $dest )`n  format: $( $format )`n  path:  $( $path )`n  filter: $( $filter )`nsetParameter: {auditAuthorizationSuccess: true}") | Set-Content -Path $mongod_conf
    Add-content $Logfile -value "$( Get-Date ): AuditLog section was uncommented and updated in $( $mongod_conf )"
} else {
    Add-Content $mongod_conf "`nauditLog:`n  destination: $( $dest )`n  format: $( $format )`n  path:  $( $path )`n  filter: $( $filter )`nsetParameter: {auditAuthorizationSuccess: true}"
    Add-content $Logfile -value "$( Get-Date ): AuditLog section was added to $( $mongod_conf )"
}

#enable error logging
if(-Not (Select-String -Path $mongod_conf -Pattern "^security" -Quiet)) {
    if (Select-String -Path $mongod_conf -Pattern "^#security" -Quiet) {
        ((Get-Content -path $mongod_conf) -replace "#security:.*","security:`n  authorization: enabled") | Set-Content -Path $mongod_conf
    } else {
        Add-Content $mongod_conf "`nsecurity:`n  authorization: enabled"
    }
}

#Restart mongod service after configuration has changed
if ($restart_mongodb -eq 1) {
    net stop MongoDB
    Start-Sleep -Seconds 2
    net start MongoDB
    Add-content $Logfile -value "$( Get-Date ): Restarted mongod."

} else {
    Add-content $Logfile -value "$( Get-Date ): Changes were made in example file. Mongod was not restarted."
}


