#Save arguments
$dest = $( $args[0] )
$format = $( $args[1] )
$path = $( $args[2] )
$filter = $( $args[3] )
$logfile = $( $args[4] )

Write-Host "$( Get-Date ): Params passed to mongod script:`n`tDEST=$( $dest )`n`tPATH=$( $path )`n`tFILTER=$( $filter )`n"

$mongod_conf = gci -recurse -filter "mongod.cfg" -File -ErrorAction SilentlyContinue
$mongod_conf



