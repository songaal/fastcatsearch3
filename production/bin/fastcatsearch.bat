@echo off
rem ------------------------------------------------------------------------------- 
rem  Copyright (C) 2011 WebSquared Inc. http://websqrd.com
rem ------------------------------------------------------------------------------- 

call %~dp0\environment.bat

set CONF=%server_home%\conf
set LIB=%server_home%\lib
set LOGS=%server_home%\logs
set OUTPUT_LOG=%LOGS%\output.log

set JVM_OPTS=-Xms%heap_memory_size% -Xmx%heap_memory_size% -XX:+HeapDumpOnOutOfMemoryError
set JAVA_OPTS=-server -Dfile.encoding=UTF-8 -Dlogback.configurationFile=%CONF%/logback.xml -Dderby.stream.error.file=%LOGS%/db.log
set ADDITIONAL_OPTS=

echo fastcatsearch start. see log at logs/system.log and output.log

%java_path% -Dserver.home=%server_home% %JVM_OPTS% %JAVA_OPTS% %ADDITIONAL_OPTS% -classpath %LIB%/fastcatsearch-server-bootstrap.jar org.fastcatsearch.server.Bootstrap start > %OUTPUT_LOG%
