<?php
$lon=$_GET['lon'];
$lat=$_GET['lat'];
$alt=$_GET['alt'];
$spd=$_GET['spd'];
$mytime=$_GET['time'];
$file="./files/coord_tmp.txt";
$txt="lat=".$lat . ",lon=" . $lon . ",alt=" . $alt . ",time=". $mytime . ",spd=" . $spd;
$size = file_put_contents($file,$txt,LOCK_EX);
rename($file, "./files/coord.txt");
//$binarydata = sprintf("%08X%08X%08X%08X", $lat, $lon, $alt, $mytime);
$lat = round($lat*10000000);
$lon = round($lon*10000000);
$alt = round($alt);
$binarydata = pack("NNNN", $lat, $lon, $alt, $mytime);
//$fileHex="./files/coord.bin.txt";
$fileHex="./files/coord_tmp.bin";
$size = file_put_contents($fileHex,$binarydata,LOCK_EX);
rename($fileHex, "./files/coord.bin");
?>
