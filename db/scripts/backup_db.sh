#!/bin/bash
# Program:
#	The script to backup the wireless_order_db 
# History:
# 2010/12/21   yzhang	First Release

#set the backup file location and file name
dir_path=~/db/backup
file_name="wireless_order_db.sql"

file_path="$dir_path""/""$file_name"
test -e $dir_path || mkdir $dir_path
test -e $file_path || touch $file_path
mysqldump -u root -pHelloZ315 wireless_order_db > $file_path