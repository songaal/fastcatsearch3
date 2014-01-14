#!/bin/sh
#-------------------------------------------------------------------------------
# Copyright (C) 2011 WebSquared Inc. http://websqrd.com
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-------------------------------------------------------------------------------
#Fastcat start script
cd `dirname $0`/../
SERVER_HOME=`pwd`

CONF=$SERVER_HOME/conf
LIB=$SERVER_HOME/lib
LOGS=$SERVER_HOME/logs
SERVER_LOG=$LOGS/server.log

# make log directory if not exists
mkdir -p $LOGS


# PROFILE
PROFILE_AGENT_LINUX_X86_32=$SERVER_HOME/bin/profile/yourkit/linux-x86-32/libyjpagent.so
PROFILE_AGENT_LINUX_X86_64=$SERVER_HOME/bin/profile/yourkit/linux-x86-64/libyjpagent.so
PROFILE_AGENT_WIN32=$SERVER_HOME/bin/profile/yourkit/win32/yjpagent.dll
PROFILE_AGENT_WIN64=$SERVER_HOME/bin/profile/yourkit/win64/yjpagent.dll
PROFILE_AGENT=PROFILE_AGENT_LINUX_X86_64
PROFILE_PORT=10001

HEAP_MEMORY_SIZE=512m
JVM_OPTS="-Xms$HEAP_MEMORY_SIZE -Xmx$HEAP_MEMORY_SIZE -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="-server -Dfile.encoding=UTF-8 -Dlogback.configurationFile=$CONF/logback.xml -Dderby.stream.error.file=logs/db.log"
DEBUG_OPT="-verbosegc -XX:+PrintGCDetails -Dcom.sun.management.jmxremote"
PROFILE_OPT="-agentpath:$PROFILE_AGENT=port=$PROFILE_PORT"

ADDITIONAL_OPTS=

if [ "$1" = "debug" ] ; then
	
	ADDITIONAL_OPTS=$DEBUG_OPT
	
elif [ "$1" = "profile" ] ; then
	
	ADDITIONAL_OPTS=$PROFILE_OPT
	
fi


if [ "$1" = "run" ] ; then

	exec java -Dserver.home=$SERVER_HOME $JVM_OPTS $JAVA_OPTS -classpath $LIB/fastcatsearch-server-bootstrap.jar org.fastcatsearch.server.Bootstrap start

elif [ "$1" = "start" ] || [ "$1" = "debug" ] || [ "$1" = "profile" ] ; then

	nohup java -Dserver.home=$SERVER_HOME $JVM_OPTS $JAVA_OPTS $ADDITIONAL_OPTS -classpath $LIB/fastcatsearch-server-bootstrap.jar org.fastcatsearch.server.Bootstrap start >> $SERVER_LOG &
	PID=`echo "$!"`
	sleep 1
	if ps -p $PID > /dev/null
	then
		echo "################################"
		echo $PID > ".pid"
		echo "Start server PID = $PID"
		echo "nohup java -Dserver.home=$SERVER_HOME $JVM_OPTS $JAVA_OPTS $ADDITIONAL_OPTS -classpath $LIB/fastcatsearch-server-bootstrap.jar org.fastcatsearch.server.Bootstrap start >> $SERVER_LOG &"
		echo "################################"
		tail -f $LOGS/system.log
	else
		echo "[ERROR] Fail to start server. Check details at logs/server.log file."
		echo "---------------------------"
		tail -1 $LOGS/server.log
		echo "---------------------------"
	fi

elif [ "$1" = "stop" ] ; then
	if [ -f ".pid" ] ; then
		PID=`cat ".pid"`
		if ps -p $PID > /dev/null
		then
			echo "################################"
			echo "Stop Daemon PID = $PID"
			ps -p "$PID"
			echo "kill $PID"
			kill "$PID"
			echo "################################"
			tail -f $LOGS/system.log
		else
			echo "Cannot find pid $PID"
		fi
	else
		echo "Cannot stop daemon: .pid file not found"
		ps -ef|grep org.fastcatsearch.server.Bootstrap|grep -v grep
	fi
	
elif [ "$1" = "kill" ] ; then
	if [ -f ".pid" ] ; then
		PID=`cat ".pid"`
		if ps -p $PID > /dev/null
		then
			echo "################################"
			echo "Kill Daemon PID = $PID"
			ps -p "$PID"
			echo "kill -9 $PID"
			kill -9 "$PID"
			echo "################################"
		else
			echo "Cannot find pid $PID"
		fi
	else
		echo "Cannot kill daemon: .pid file not found"
		ps -ef|grep org.fastcatsearch.server.Bootstrap|grep -v grep
	fi
	
elif [ -z "$1" ] ; then
	
	echo "usage: $0 run | start | stop | debug | profile"
	
fi