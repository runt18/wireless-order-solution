#!/bin/bash
# Program:
# 	The script to backup the eMenu's images and have them moved to remote computer.
# History
# 2013/1/26	yzhang	First Release


# set the file path
src_dir=~/www/eMenu/
dest_dir=~/www/eMenu/
dest_file="images.tar.gz"
host="122.115.57.66"
user="yzhang"

# gzip the image files
echo zipped the image files under $src_dir/images...
cd $src_dir
tar -zcf $dest_dir$dest_file images

# move the image files to remote computer
echo move to $host using account $user...
scp -l 800 $dest_dir$dest_file $user@$host:$dest_dir$dest_file 

# delete the gzipped image files
echo delete the $dest_dir$dest_file...
rm -f $dest_dir$dest_file
