$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

#Load write-host from configuration file
$configfile = "C:\Users\OferHaim\universal-connector\mongodb-server-configuration\windows\configureServer.conf"
$dest = ([string](Select-String -Path $configfile -Pattern "destination")).Split(":")[-1].Trim()
$format = ([string](Select-String -Path $configfile -Pattern "format")).Split(":")[-1].Trim()
$path = "C:$(([string](Select-String -Path $configfile -Pattern "path")).Split(":")[-1].Trim())"
$filter = [string](Select-String -Path $configfile -Pattern "filter")
$filter = $filter.Substring($filter.IndexOf("filter:") + 7).Trim()
$host_addresses = [string](Select-String -Path $configfile -Pattern "host-addresses")
$host_addresses = $host_addresses.Substring($host_addresses.IndexOf("host-addresses:") + 15).Trim()
$enable_loadbalance = 1
$restart_mongodb = 1

#Load flags
$i = 0;
while ($i -lt $args.count)
{
    switch ($args[$i])
    {
        { ($_ -eq "-h") -or ($_ -eq "--help") } {
            write-host "This script has 2 purposes:"
            write-host "1. Configure Mongodb to send audit logs to Filebeat"
            write-host "2. Configure Filebeat to send logs to Guardium Universal Connector"
            write-host "Optional flags:"
            write-host "-h, --help                Show brief help"
            write-host "-f, --filter	          Specify a filter for mongodb auditLog"
            write-host "--host-addresses       	  Specify addresses to send data seperated by comma. for example: <ip_address1>:<port1>,<ip_address2>:<port2>"
            write-host "--enable-loadbalance      Enable Filebeat loadbalance."
            write-host "--restart-mongodb      	  Changes MongoDB configuration file, DB restart will be performed."
            exit;
        }
        { ($_ -eq "-f") -or ($_ -eq "--filter") } {
            if ($i -lt $args.count - 1)
            {
                $filter = $args[$i + 1]
                $i++
            }
            else
            {
                write-host "no filter specified"
                exit 1
            }
            break;
        }
        { ($_ -eq "--host-address") } {
            if ($i -lt $args.count - 1)
            {
                $host_addresses = $args[$i + 1]
                $i++
            }
            else
            {
                write-host "no host-addresses specified"
                exit 1
            }
            break;
        }
        { ($_ -eq "--enable-loadbalance") } {
            $enable_loadbalance = 1
        }
        { ($_ -eq "--restart-mongodb") } {
            $restart_mongodb = 1
        }


    }
    $i++;
}

#Write-Host "$(Get-Date): mongod params:`n`tDEST=$($dest)`n`tFORMAT=$($format)`n`tPATH=$($path)`n`tFILTER=$($filter)`n"
#Write-Host "$(Get-Date): filebeat params:`n`tPATH=$($path)`n`tHOST_ADDRESSES=$($host_addresses)`n`tENABLE_LOADBALANCE=$($enable_loadbalance)`n"

if ($restart_mongodb -eq 1)
{
    Write-Host "Configuring MongoDB auditLog..."
    ."$scriptDir\configureMongodb.ps1" $dest $format $path $filter $restart_mongodb
}

#Add-Content $logfile -value "$(Get-Date): Configuring filebeat..."
#."$scriptDir\configureFilebeat.ps1" $path $host_addresses $enable_loadbalance
Write-Host "$(Get-Date): Done configuring Server."
