#!/bin/bash
# Program:
#	The program runs the socket using JVM
# History:
# 2011/12/10	yzhang	First Release 

home_dir="/home/yzhang"

cur_dir=$(pwd);
cd /home/yzhang/socket
is_cronolog_on=$(which cronolog | grep -c "cronolog")
if [ "$is_cronolog_on" -eq 1 ]; then
	echo "java -Duser.timezone=Asia/Shanghai -jar wireless_order_socket.jar conf.xml 2>&1 | cronolog ${home_dir}/socket/log/errors_%Y-%m-%d.log" | at now
else
	echo "java -Duser.timezone=Asia/Shanghai -jar wireless_order_socket.jar conf.xml" | at now
	echo 'WARNING!!! The socket server does NOT log any error since cronolog NOT be found.'
fi
cd "$cur_dir"
