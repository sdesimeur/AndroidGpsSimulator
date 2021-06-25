#!/bin/bash

mkdir ~/run/

LOCKFILE=~/run/virtualFlight.lock

if [ -e ${LOCKFILE} ];
then 
	exit 0;
fi

touch ${LOCKFILE}

trap "rm ${LOCKFILE}" 2 3 

FILE="$HOME/public_html/AndroidGpsSimulator/MapMockGpsPHP/tracking/files/coord.txt"
FILEMAP="../Debug/SysNavXLMasterSTM32L552.map"

export IFS=","

while /bin/true;
do
	#VALUES=($(cat ${FILE}))
	sleep 1s
	if [ ! -f "${FILEMAP}" ];
	then
		echo "nothing"
		continue
	fi
	ADDR=$(grep "^\s*0x[0-9a-fA-F]*\s*GPS_Virtual\s*$" ${FILEMAP} | sed "s/^\s*0x0*2\([0-9a-fA-F]*\)\s*GPS_Virtual\s*$/0x2\1/")
	eval $(cat ${FILE} | sed "s/,/;/g")
	tmp=$(echo "${lat} * 10000000" | bc); lat=${tmp%.*}
	tmp=$(echo "${lng} * 10000000" | bc); lng=${tmp%.*}
	tmp=$(echo "${spd} * 10" | bc); spd=${tmp%.*}
	tmp=$(echo "${hdg}" | bc); hdg=${tmp%.*}
	echo -e -n "mww $((${ADDR})) ${lat}\n" \
		"mww $((${ADDR} + 4)) ${lng}\n" \
		"mww $((${ADDR} + 8)) ${time}\n" \
		"mwh $((${ADDR} + 12)) ${alt}\n" \
		"mwh $((${ADDR} + 14)) ${hdg}\n" \
		"mwh $((${ADDR} + 16)) ${spd}\n" \
		"exit\n" | netcat 127.0.0.1 64444
done
