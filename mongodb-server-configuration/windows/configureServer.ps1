$logfile = "C:\Users\OferHaim\universal-connector\mongodb-server-configuration\configureServer.log"
#Load write-host from configuration file
$configfile = "C:\Users\OferHaim\universal-connector\mongodb-server-configuration\configureServer.conf"
$dest = ([string](Select-String -Path $configfile -Pattern "destination")).Split(":")[-1].Trim()
$format = ([string](Select-String -Path $configfile -Pattern "format")).Split(":")[-1].Trim()
$path = ([string](Select-String -Path $configfile -Pattern "path")).Split(":")[-1].Trim()
$filter = [string](Select-String -Path $configfile -Pattern "filter")
$filter = $filter.Substring($filter.IndexOf("filter:") + 7).Trim()
$protocol = ([string](Select-String -Path $configfile -Pattern "protocol")).Split(":")[-1].Trim()
$address = [string](Select-String -Path $configfile -Pattern "address")
$address = $address.Substring($address.IndexOf("address:") + 8).Trim()

#Load flags
$i = 0;
while ($i -lt $args.count)
{
    switch ($args[$i])
    {
        { ($_ -eq "-h") -or ($_ -eq "--help") } {
            write-host "options:"
            write-host "-h, --help                show brief help"
            write-host "-d, --destination         specify a destination for mongodb: syslog or file"
            write-host "-f, --filter	          specify a filter for mongodb auditLog"
            write-host "-a, --address       	  specify an address to send data <ip_address>:<port>"
            write-host "-p, --protocol       	  specify the sent protocol: tcp or udp"
            write-host "--syslog-only       	  update only syslog configuration file, without changing mongodb"
            exit;
        }
        { ($_ -eq "-d") -or ($_ -eq "--destination") } {
            if ($i -lt $args.count - 1)
            {
                $dest = $args[$i + 1]
                $i++
            }
            else
            {
                write-host "no destination specified"
                exit 1
            }
            break;
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
        { ($_ -eq "-a") -or ($_ -eq "--address") } {
            if ($i -lt $args.count - 1)
            {
                $address = $args[$i + 1]
                $i++
            }
            else
            {
                write-host "no address specified"
                exit 1
            }
            break;
        }
        { ($_ -eq "-p") -or ($_ -eq "--protocol") } {
            if ($i -lt $args.count - 1)
            {
                $protocol = $args[$i + 1]
                $i++
            }
            else
            {
                write-host "no protocol specified"
                exit 1
            }
            break;
        }
        { ($_ -eq "--syslog-only") } {
            $syslog_only = $true
        }


    }
    $i++;
}

Add-Content $logfile -value "$(Get-Date): mongod params:`n`tDEST=$($dest)`n`tPATH=$($path)`n`tFILTER=$($filter)`n"
Add-Content $logfile -value "$(Get-Date): rsyslog params:`n`tPROTOCOL=$($protocol)`n`tADDRESS=$($address)`n"

if (-Not $syslog_only)
{
    Add-Content $logfile -value "Configuring MongoDB auditLog..."
    ."C:\Users\OferHaim\universal-connector\mongodb-server-configuration\windows\configureMongodb.ps1" $dest $format $path $filter $logfile
}

Add-Content $logfile -value "$(Get-Date): Configuring syslog..."
."C:\Users\OferHaim\universal-connector\mongodb-server-configuration\windows\configureSyslog.ps1" $protocol $address $logfile
Add-Content $logfile -value "$(Get-Date): Done configuring Server."
Write-Host "$(Get-Date): Done configuring Server."