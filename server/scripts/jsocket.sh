#!/bin/bash
# Program:
#	The program runs the socket using JVM
# History:
# 2011/12/10	yzhang	First Release 

home_dir="/home/yzhang"

cur_dir=$(pwd);
cd /home/yzhang/socket
echo java -Duser.timezone=Asia/Shanghai -jar $home_dir/socket/wireless_order_socket.jar $home_dir/socket/conf.xml | at now
cd "$cur_dir"
