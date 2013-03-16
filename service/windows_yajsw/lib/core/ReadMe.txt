#-------------------------------------------------------------------------------
# Copyright (c) 2013 Websquared, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the GNU Public License v2.0
# which accompanies this distribution, and is available at
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
# 
# Contributors:
#     swsong - initial API and implementation
#-------------------------------------------------------------------------------
This is the minimal set of libraries required by YAJSW

It includes the following core functionalities:

All platforms
	* runConsole
	* filter triggers
	* regex filter triggers
	* logging
	* restart on exit code
	* groovy scripting
	* network start
	
Windows
	* installService, uninstallService, startService, stopService
	
It does not include the following functions:

	* installDaemon, uninstallDaemon, startDaemon, stopDaemon
	* system tray icon
	* timers
	* services manager
	* network start per webdav or http or ftp
