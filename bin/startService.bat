cd %~dp0\..\service\windows_yajsw
call setenv.bat
%wrapper_bat% -t %conf_file%
pause


