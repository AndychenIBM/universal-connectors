$filePath = $( $args[0] )
$strToReplace = $( $args[1] )
$replaceStr = $( $args[2] )
$startRange = $( $args[3] )
$endRange = $( $args[4] )

#Write-Host "$( Get-Date ): Params passed to replaceInLinesRange script:`n`tPATH=$( $filePath )`n`tstrToReplace=$( $strToReplace )`n`treplaceStr=$( $replaceStr )`n`tstartRange=$( $startRange )`n`tendRange=$( $endRange )"

(Get-Content $filePath) | Foreach-Object {
    if ($_.ReadCount -ge $startRange -and $_.ReadCount -le $endRange) {
        $_ -replace $strToReplace, $replaceStr
    } else {
        $_
    }
} | Set-Content $filePath