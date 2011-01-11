#!/bin/bash
# Program:
#	The program perform two tasks.
#	1 - set the timezone to "Asia/Shanghai" 
#	2 - run the wireless order socket 
# History:
# 2010/08/03    yzhang	First Release
order_socket=$(ps aux | grep -c "wireless_order_socket.jar")
if [ "$order_socket" -eq 1 ]; then
	sudo ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
	cur_dir=$(pwd);
	cd ~/socket
	echo 'java -Duser.timezone=Asia/Shanghai -jar ~/socket/wireless_order_socket.jar ~/socket/conf.xml' | at now
	cd "$cur_dir"
	echo -e 'Start the wireless order socket.'
else
	echo -e 'Wireless order socket has been running.'
fi
exit 0
