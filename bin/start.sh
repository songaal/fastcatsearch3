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
if [ -n "$FASTCAT_HOME" ]; then IR_HOME=$FASTCAT_HOME; else IR_HOME=`pwd`; fi

CONF=$IR_HOME/conf
LIB=$IR_HOME/lib
JSP_LIB=$LIB/jsp/commons-el.jar:$LIB/jsp/jasper-compiler-5.5.15.jar:$LIB/jsp/jasper-compiler-jdt-5.5.15.jar:$LIB/jsp/jasper-runtime-5.5.15.jar:$LIB/jsp/jsp-api-2.0.jar

#for background service
FASTCAT_CLASSPATH=".:bin"
for jarfile in `find $LIB | grep [.]jar$`; do FASTCAT_CLASSPATH="$FASTCAT_CLASSPATH:$jarfile"; done
nohup java -Dserver.home=$IR_HOME -Xmx512m -server -Dfile.encoding=UTF-8 -Dlogback.configurationFile=$CONF/logback.xml -Dderby.stream.error.file=logs/db.log -classpath $FASTCAT_CLASSPATH org.fastcatsearch.server.CatServer 2>&1 &
