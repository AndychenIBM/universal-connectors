$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Logfile = "$scriptDir\GUC.log"

#Load flags
$i = 0;
$enable_loadbalance = 0
$restart_mongodb = 0
$dest = "file"
$format = "JSON"
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
            write-host "--path	                  Specify a path for saving mongodb auditLog"
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
                Add-content $Logfile -value "$(Get-Date):-f/--filter flag was entered but no filter was specified."
                exit 1
            }
            break;
        }
        { ($_ -eq "--host-address") } {
            if ($i -lt $args.count - 1)
            {
                $host_addresses = $args[$i + 1] -replace '_', ':'
                $i++
            }
            else
            {
                write-host "no host-addresses specified"
                Add-content $Logfile -value "$(Get-Date):--host-address flag was entered but no host-addresses were specified."
                exit 1
            }
            break;
        }
        { ($_ -eq "--path") } {
            if ($i -lt $args.count - 1)
            {
                $path = $args[$i + 1]
                $i++
            }
            else
            {
                write-host "no path specified"
                Add-content $Logfile -value "$(Get-Date):--path flag was entered but no path was specified."
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

if (-Not (Test-Path variable:path)){
    write-host "No path specified. Exiting configureServer script."
    Add-content $Logfile -value "$(Get-Date): path variable cannot remain empty. Exiting configureServer script."
    exit 1
}

if (-Not (Test-Path variable:host_addresses)){
    write-host "No host addresses specified. Exiting configureServer script."
    Add-content $Logfile -value "$(Get-Date): host_addresses variable cannot remain empty. Exiting configureServer script."
    exit 1
}

Add-content $Logfile -value "$(Get-Date): Configuring MongoDB auditLog..."
."$scriptDir\configureMongodb.ps1" $dest $format $path $filter $restart_mongodb


#Add-Content $logfile -value "$(Get-Date): Configuring filebeat..."
."$scriptDir\configureFilebeat.ps1" $path $host_addresses $enable_loadbalance
Add-content $Logfile -value "$(Get-Date): Done configuring Server."
