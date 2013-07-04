@echo off
rem -----------------------------------------------------------------------------
rem 이 셋팅은 사용자가 수정할 경우, 문제가 생길수 있습니다.
rem 검색엔진 설정은 searchenv.bat(sh)을 사용해주시기 바랍니다. 
rem 공백이 포함된 경로를 올바로 표시하기 위해서는 쌍따옴표로 묶어주세요.
rem -----------------------------------------------------------------------------

rem 디폴트 java home
set wrapper_home=%~dp0

rem Wrapper를 실행시키기 위한 디폴트 java실행파일
rem 이것은 검색엔진을 실행하기 위한 java실행파일이 아니다. 검색엔진을 위한 java는 wrapper 설정파일에 정의한다.
set java_exe="java"
set javaw_exe="javaw"

rem wrapper.jar파일의 위치. yajsw의 필수 lib파일들은 이 jar에 의해서 로딩되며, <wrapper_home>/lib/ 하위에 존재해야 한다.
set wrapper_jar="%wrapper_home%wrapper.jar"

rem wrapper process를 위한 java 옵션값. 
set wrapper_java_options=-Xmx30m

rem wrapper를 실행하기 위한 wrapper 배치파일위치
set wrapper_bat="%wrapper_home%wrapper.bat"

rem 모든 배치파일에서 참조되는 wrapper.conf 파일
set conf_file="%wrapper_home%wrapper.conf"

rem 검색엔진 설정을 읽어온다.
echo call %wrapper_home%..\..\exec\searchenv.bat
call %wrapper_home%..\..\exec\searchenv.bat

rem wrapper.conf 파일을 설정에 자동 Generate해준다.
rem 사용법: ConfWriter [기록할 wrapper.conf파일위치] [검색엔진홈] [검색엔진java옵션] [OS사용자아이디] [사용자비밀번호]
java -classpath ..\yajsw_conf.jar com.websqrd.fastcat.yajsw.ConfWriter wrapper.conf ..\..\ %yajsw_java_options% %yajsw_account_user% %yajsw_account_password%



