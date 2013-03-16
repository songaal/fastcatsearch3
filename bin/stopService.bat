cd %~dp0\..\service\windows_yajsw
call setenv.bat
%wrapper_bat% -p %conf_file%
pause


