<?php
$lon=$_GET['lon'];
$lat=$_GET['lat'];
$alt=$_GET['alt'];
$file="./files/coord.txt";
$txt="lat=".$lat . ",lon=" . $lon . ",alt=" . $alt;
$size = file_put_contents($file,$txt,LOCK_EX);
//$binarydata = sprintf("%08X%08X%08X", $lat, $lon, $alt);
$binarydata = pack("NNN", $lat, $lon, $alt);
//$fileHex="./files/coord.bin.txt";
$fileHex="./files/coord.bin";
$size = file_put_contents($fileHex,$binarydata,LOCK_EX);
?>
