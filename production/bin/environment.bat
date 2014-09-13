@echo off
echo '++++++++++ Fastcatsearch Environment ++++++++++'

set heap_memory_size=512m
set java_path=java

pushd  %~dp0\..
set server_home=%cd%
echo server_home = %server_home%
popd