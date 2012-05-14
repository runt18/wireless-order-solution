#!/bin/bash
# Program:
# 	The script to backup the wireless_order_db and have it moved to remote
#	computer.
# History
# 2012/5/14	yzhang	First Release

sh ~/db/scripts/backup_db.sh       

# set the file path
dir=~/db/backup/
file="wireless_order_db.sql"
host="122.49.20.170"
user="yzhang"

# gzip the db dump file
echo zipped the $file...
gzip -fc $dir$file > $dir$file.gz

# move the dump file to remote computer
echo move to $host using account $user...
scp -l 800 $dir$file.gz $user@$host:$dir$file.gz 

# delete the gzipped dump file
echo delete the $file.gz...
rm -f $dir$file.gz
