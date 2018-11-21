<?php
$lon=$_GET['lon'];
$lat=$_GET['lat'];
$alt=$_GET['alt'];
$file="./files/coord_tmp.txt";
$txt="lat=".$lat . ",lon=" . $lon . ",alt=" . $alt;
$size = file_put_contents($file,$txt,LOCK_EX);
rename($file, "./files/coord.txt");
//$binarydata = sprintf("%08X%08X%08X", $lat, $lon, $alt);
$lat = round($lat*10000000);
$lon = round($lon*10000000);
$alt = round($alt);
$binarydata = pack("NNN", $lat, $lon, $alt);
//$fileHex="./files/coord.bin.txt";
$fileHex="./files/coord_tmp.bin";
$size = file_put_contents($fileHex,$binarydata,LOCK_EX);
rename($fileHex, "./files/coord.bin");
?>
