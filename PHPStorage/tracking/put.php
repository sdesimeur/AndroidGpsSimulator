<?php
$lon=$_GET['lon'];
$lat=$_GET['lat'];
$file="./files/coord.txt";
//$file="/tmp/coord.txt";
$txt="lat=".$lat . ",lon=" . $lon;
file_put_contents($file,$txt,LOCK_EX);
?>
