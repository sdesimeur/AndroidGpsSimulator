You have to copy the directory /tracking/ which is in /PHPStorage/ directory on a Web PHP Server.

You have to install MockGpsProvider apk on the android's device which have to receive fake GPS position.

You have to choise between MapMockGps or MapMockGpsPHP:
*MapMockGps
Create some directories named /sdesimeur/mapsforge/ in SD card of the android's device  which have to create and send fake GPS position, download an OSM map with .map extension (for example: http://download.mapsforge.org/maps/europe/france/picardie.map) and copy it as /sdesimeur/mapsforge/local.map
Then you have to install MapMockGps on this android's device.

*MapMockGpsPHP
You have to install it on a Web PHP Server.

MockGpsProvider and, MapMockGps or MapMockGpsPHP have to point to the web page where the /tracking/ directory can be found.

