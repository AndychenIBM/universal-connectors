$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Logfile = "$scriptDir\GUC.log"
$FILEBEAT_DEFAULT_CONF_PATH="C:\Users\Administrator\Downloads\filebeat\filebeat.yml"

#Save arguments
$path = $( $args[0] )
$host_addresses = $( $args[1] )
$enable_loadbalance = $( $args[2] )

Add-content $Logfile -value "$( Get-Date ): Params passed to filebeat script:`n`tPATH=$( $path )`n`tHOST_ADDRESSES=$( $host_addresses )`n`tENABLE_LOADBALANCE=$( $enable_loadbalance )"

#Find filebeat configuration file path
$filebeat_conf=Get-Variable -Name FILEBEAT_CONF_PATH -Scope Global -ErrorAction SilentlyContinue
if($filebeat_conf -eq $null){
    $filebeat_conf = $FILEBEAT_DEFAULT_CONF_PATH
}

Add-content $Logfile -value "$( Get-Date ): Filebeat configuration file path is: $( $filebeat_conf )"

#Configure Filebeat input:
$startRange = (Get-Content $filebeat_conf | select-string "filebeat.inputs:").LineNumber
$endRange = $startRange + 30
."$scriptDir\replaceInLinesRange.ps1" $filebeat_conf "  enabled: false" "  enabled: true" $startRange $endRange
if (Select-String -Path $filebeat_conf -Pattern "$( $path )" -SimpleMatch -Quiet)
{
    Add-content $Logfile -value "$( Get-Date ): path is already located in filebeat configuration. Please check: $( $filebeat_conf )`n"
}
else
{
    ."$scriptDir\replaceInLinesRange.ps1" $filebeat_conf "  paths:" "  paths:`r`n    - $( $path )" $startRange $endRange
    Add-content $Logfile -value "$( Get-Date ): path was entered to filebeat configuration. `n"
}

#Configure Filebeat output:
(Get-Content $filebeat_conf) -replace "#output.logstash:", "output.logstash:" | Set-Content $filebeat_conf
$startRange = (Get-Content $filebeat_conf | select-string "output.logstash:").LineNumber
$endRange = $startRange + 30

$replace_string = "hosts: [`"$( $host_addresses -replace ",", '","' )`"]"
if (Select-String -Path $filebeat_conf -Pattern "$( $replace_string )" -SimpleMatch -Quiet)
{
    Add-content $Logfile -value "$( Get-Date ): All host addresses are already located in filebeat configuration. Please check: $( $filebeat_conf )`n"
}
else
{
    if (($enable_loadbalance -eq 1) -And -Not(Select-String -Path $filebeat_conf -Pattern "loadbalance: true" -SimpleMatch -Quiet))
    {
        $replace_string = "$( $replace_string )`n  loadbalance: true"
    }

    if (Select-String -Path $filebeat_conf -Pattern "#hosts:" -SimpleMatch -Quiet)
    {
        ."$scriptDir\replaceInLinesRange.ps1" $filebeat_conf "#hosts:.*" $replace_string $startRange $endRange
        Add-content $Logfile -value "$( Get-Date ): Uncommented and updated host addresses in filebeat configuration."
    }

    if (-Not( Select-String -Path $filebeat_conf -Pattern "$( $replace_string )" -SimpleMatch -Quiet) -And (Select-String -Path $filebeat_conf -Pattern "hosts:" -SimpleMatch -Quiet))
    {
        ."$scriptDir\replaceInLinesRange.ps1" $filebeat_conf "hosts:.*" $replace_string $startRange $endRange
        Add-content $Logfile -value "$( Get-Date ): Updated host addresses in filebeat configuration."
    }
}

Restart-Service filebeat
Add-content $Logfile -value "$( Get-Date ): Restarted filebeat."
