#!/bin/sh
cd `dirname $0`/../
SERVER_HOME=`pwd`
java -classpath $SERVER_HOME/lib/fastcatsearch-server-2.14.3.jar org.fastcatsearch.util.PingTest "$@"