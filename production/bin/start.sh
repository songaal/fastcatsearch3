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

# PROFILE
PROFILE_AGENT_LINUX_X86_32=$SERVER_HOME/bin/profile/yourkit/linux-x86-32/libyjpagent.so
PROFILE_AGENT_LINUX_X86_64=$SERVER_HOME/bin/profile/yourkit/linux-x86-64/libyjpagent.so
PROFILE_AGENT=PROFILE_AGENT_LINUX_X86_64
PROFILE_PORT=10001

HEAP_MEMORY_SIZE=512m
JVM_OPTS="-Xms$HEAP_MEMORY_SIZE -Xmx$HEAP_MEMORY_SIZE -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="-server -Dfile.encoding=UTF-8 -Dlogback.configurationFile=$CONF/logback.xml -Dderby.stream.error.file=logs/db.log"
DEBUG_OPT="-verbosegc -XX:+PrintGCDetails -Dcom.sun.management.jmxremote"
PROFILE_OPT="-agentpath:$PROFILE_AGENT=port=$PROFILE_PORT"

#for background service
FASTCAT_CLASSPATH=".:bin"
for jarfile in `find $LIB | grep [.]jar$`; do FASTCAT_CLASSPATH="$FASTCAT_CLASSPATH:$jarfile"; done
nohup java $JVM_OPTS $JAVA_OPTS -classpath $FASTCAT_CLASSPATH org.fastcatsearch.server.CatServer $SERVER_HOME 2>&1 &
