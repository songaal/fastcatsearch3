#!/bin/bash

echo '++++++++++ Fastcatsearch Environment ++++++++++'

java_path=
daemon_account=fastcat

if [ -z "$heap_memory_size" ]; then
    heap_memory_size=768m
fi

current=$( dirname "$0" )

cd $current/../
server_home=$(pwd)
# return to original folder
cd "$current"

export heap_memory_size
export server_home
export java_path
export daemon_account

echo server_home = "$server_home"
echo heap_memory_size = "$heap_memory_size"
