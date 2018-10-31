<?php
$file="./files/coord.txt";
$txt=file_get_contents($file);
Header ("Content-type: text/plain; charset=utf-8");
$tab=explode(",",$txt);
$tab1=str_replace(array("lat=","lon=","alt="),"",$tab);
$type=$_GET["type"];
if ($type=="lat") {
	echo $tab1[0];
} elseif ($type=="lon") {
	echo $tab1[1];
} elseif ($type=="alt") {
	echo $tab1[2];
} else {
	echo ";" . $txt . ";";
}
?>
