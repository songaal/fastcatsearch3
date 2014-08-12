#!/bin/sh

# ------------------------------------------------------------------------------
# YourKit Java Profiler startup script
# ------------------------------------------------------------------------------

_YH_=`dirname "$0"`/..

if [ ! -z "$YJP_JAVA_HOME" ] ; then
  JAVA_EXE="$YJP_JAVA_HOME/bin/java"
  TOOLS_JAR_OPTION="-Xbootclasspath/a:$YJP_JAVA_HOME/lib/tools.jar"
elif [ "`uname -a | grep HP-UX`" ] ; then
  JAVA_EXE="/opt/java6/bin/java"
  TOOLS_JAR_OPTION="-Xbootclasspath/a:/opt/java6/lib/tools.jar"
elif [ "`uname | grep SunOS`" ] ; then
  JAVA_EXE="/usr/jdk/latest/bin/java"
  TOOLS_JAR_OPTION="-Xbootclasspath/a:/usr/jdk/latest/lib/tools.jar"
elif [ "`uname | grep FreeBSD`" ] ; then
  JAVA_EXE="/usr/local/openjdk7/bin/java"
  TOOLS_JAR_OPTION="-Xbootclasspath/a:/usr/local/openjdk7/lib/tools.jar"
elif [ "`uname -a | grep Linux`" ] ; then
  # Any Linux
  if [ "`uname -m | grep x86_64`" ] || [ "`uname -i | grep 86`" ] ; then
    # Intel
    if [ "`getconf LONG_BIT | grep 64`" ] ; then
      JAVA_EXE="$_YH_/jre64/bin/java"
      TOOLS_JAR_OPTION="-Xbootclasspath/a:$_YH_/lib/tools.jar"
    else
      JAVA_EXE="$_YH_/jre/bin/java"
      TOOLS_JAR_OPTION="-Xbootclasspath/a:$_YH_/lib/tools.jar"
    fi
  fi
fi

if [ ! -r "$JAVA_EXE" ] && [ ! -z "$JAVA_HOME" ] ; then
  JAVA_EXE="$JAVA_HOME/bin/java"
  TOOLS_JAR_OPTION="-Xbootclasspath/a:$JAVA_HOME/lib/tools.jar"
fi

if [ ! -r "$JAVA_EXE" ] ; then
  JAVA_EXE=java
  TOOLS_JAR_OPTION=""

  _X="`which $JAVA_EXE`"
  if [ ! -z "$_X" ] ; then
    # resolve the symlink
    _X="`readlink -f $_X`"
    if [ ! -z "$_X" ] ; then
      # get the directory name
      _X="`dirname $_X`"
      if [ ! -z "$_X" ] ; then
        _X="`readlink -f $_X/../../lib/tools.jar`"
        if [ -r "$_X" ] ; then
          TOOLS_JAR_OPTION="-Xbootclasspath/a:$_X"
        fi
      fi
    fi
  fi
fi

JAVA_VERSION="`$JAVA_EXE -version 2>&1 | grep version`"
if [ -z "$JAVA_VERSION" ] ; then
  echo "Cannot find Java 6 or newer to run YourKit Java Profiler."
  echo "Java search priority:"
  echo " - environment variable YJP_JAVA_HOME, if set;"
  if [ "`uname -a | grep Linux`" ] ; then
    if [ "`uname -m | grep x86_64`" ] || [ "`uname -i | grep 86`" ] ; then
      echo " - bundled JRE, if exists;"
    fi
  elif [ "`uname -a | grep HP-UX`" ] || [ "`uname | grep SunOS`" ] ; then
    echo " - system default Java, if available;"
  fi
  echo " - environment variable JAVA_HOME, if set;"
  echo " - 'java' in PATH, if found."
  exit
fi

JAVA_TOOL_OPTIONS=
export JAVA_TOOL_OPTIONS

# On Solaris and HP-UX, use 64-bit JVM, if available
if [ "`uname | grep SunOS`" ] || [ "`uname -a | grep HP-UX`" ] ; then
  if [ "`$JAVA_EXE -d64 -version 2>&1 | grep 64-Bit`" ] ; then
    JAVA_EXE="$JAVA_EXE -d64"
  fi
fi

if [ "`$JAVA_EXE -version 2>&1 | grep 64-Bit`" ] ; then
  # 64-Bit Java
  JAVA_HEAP_LIMIT="-Xmx4G -XX:PermSize=256m -XX:MaxPermSize=256m"
else
  # 32-Bit Java
  JAVA_HEAP_LIMIT="-Xmx512m -XX:PermSize=64m -XX:MaxPermSize=64m"
fi

# If you use Xmonad window manager, uncomment next 3 lines:
# _JAVA_AWT_WM_NONREPARENTING=1
# export _JAVA_AWT_WM_NONREPARENTING
# wmname LG3D

INI_PARAMS="`cat $_YH_/bin/yjp.ini | grep -v '#'`"
exec $JAVA_EXE $JAVA_HEAP_LIMIT $INI_PARAMS $TOOLS_JAR_OPTION -jar "$_YH_/lib/yjp.jar" $1 $2 $3 $4 $5 $6 $7 $8 $9
