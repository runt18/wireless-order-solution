#!/bin/bash
# Program:
#	Monitor the status of the wireless order socket	
# History:
# 2010/10/21	yzhang	First release

order_socket=$(ps aux | grep -c "wireless_order_socket.jar")
if [ "$order_socket" -gt 1 ]; then
	#define the path to script monitor.sh
	file_path=~/socket/scripts

	"$file_path"/monitor.sh "start_monitor -t 1000"
	cur_date=$(date)
	while [ 1 ]
	do
		clear
		echo -e "The monitor run on $cur_date"
		if [ -f ~/socket/log/status.log ]; then
			cat ~/socket/log/status.log
		fi
		read -n 1 -t 1 cmd
		if [ "$cmd" == "q" ]; then
			clear
			"$file_path"/monitor.sh kill_monitor
			exit 0
		fi
	done
else
	echo -e "The monitor doesn't work due to the wireless socket isn't running."
fi



