<?php
// sign up to "https://www.mapbox.com/studio/signin/?path=%2Fstudio%2Faccount%2Fapps"
// Change:
$myId="your.mapbox.project.id";
$myAccessToken="your.mapbox.public.access.token";
//
//include "./config.php";
?>
<!DOCTYPE html>
<html>
<head>
<title>Leaflet Quick Start Guide Example</title>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.css" />
<link rel="stylesheet" href="perso.css" />
</head>
<body>
<script type="text/javascript" src="./jquery-1.7.2.min.js"></script>
ServerString : <INPUT TYPE=TEXT NAME='serverstring' ID='serverstring' SIZE=40>&nbsp;&nbsp;&nbsp;<input type="button" id="clear" value="Clear" onclick="clearMap()"><BR>
<div id="map" style="width: 800px; height: 600px"></div>
<script src="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.js"></script>
<script>
$('#serverstring').val(window.location.href);
var pos=L.latLng(49.59266, 3.65649);
var map=L.map('map').setView(pos, 13);
var line=L.polyline([pos]);
line.addTo(map);

var osmUrl = 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
	osmAttrib = '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors',
	osm = L.tileLayer(osmUrl, {maxZoom: 18, attribution: osmAttrib});
map.addLayer(osm);



/*L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
	attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
	maxZoom: 18,
	id: '<?php echo $myId; ?>',
	accessToken: '<?php echo $myAccessToken; ?>'
	}).addTo(map);*/
/*L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6IjZjNmRjNzk3ZmE2MTcwOTEwMGY0MzU3YjUzOWFmNWZhIn0.Y8bhBaUMqFiPrDRW9hieoQ', {
	maxZoom: 18,
	attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
		'<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
		'Imagery © <a href="http://mapbox.com">Mapbox</a>',
		id: 'mapbox.streets'
}).addTo(map);*/
var marker = L.marker(pos);
marker.addTo(map);
/*	.bindPopup("<b>Hello world!</b><br />I am a popup.").openPopup();
L.circle([51.508, -0.11], 500, {
	color: 'red',
	fillColor: '#f03',
	fillOpacity: 0.5
}).addTo(map).bindPopup("I am a circle.");
L.polygon([
	[51.509, -0.08],
	[51.503, -0.06],
	[51.51, -0.047]
]).addTo(map).bindPopup("I am a polygon.");
*/
//var popup = L.popup();
function onMapClick(e) {
    pos=e.latlng;
	map.panTo(pos);
	line.addLatLng(pos);
	marker.setLatLng(pos);
/*	popup
		.setLatLng(pos)
		.setContent("You clicked the map at " + pos.toString())
		.openOn(map);*/
	var server = $('#serverstring').val();
	var url = server +  "/tracking/put.php?lat=" + pos.lat + "&lon=" + pos.lng;
	var donnees = document.getElementById("donnees");
	if (window.XMLHttpRequest) {
		requete = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		requete = new ActiveXObject("Microsoft.XMLHTTP");
	}
	requete.open("GET", url, true);
//	requete.onreadystatechange = majIHM;
	requete.send(null);
}
function clearMap () {
    line.setLatLngs({});
	line.addLatLng(pos);
}
map.on('click', onMapClick);
document.getElementById('map').style.cursor = ''
</script>
</body>
</html>

