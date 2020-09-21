<?php
$lng=$_GET['lng'];
$lat=$_GET['lat'];
$alt=$_GET['alt'];
$spd=$_GET['spd'];
$hdg=$_GET['hdg'];
$mytime=$_GET['time'];
$file="./files/coord_tmp.txt";
$txt="lat=".$lat . ",lng=" . $lng . ",alt=" . $alt . ",time=". $mytime . ",spd=" . $spd . ",hdg=" . $hdg;
$size = file_put_contents($file,$txt,LOCK_EX);
rename($file, "./files/coord.txt");
//$binarydata = sprintf("%08X%08X%08X%08X", $lat, $lng, $alt, $mytime);
$lat = round($lat*10000000);
$lng = round($lng*10000000);
$alt = round($alt);
$binarydata = pack("NNNN", $lat, $lng, $alt, $mytime);
//$fileHex="./files/coord.bin.txt";
$fileHex="./files/coord_tmp.bin";
$size = file_put_contents($fileHex,$binarydata,LOCK_EX);
rename($fileHex, "./files/coord.bin");
?>
