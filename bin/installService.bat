cd %~dp0\..\service\windows_yajsw
call setenv.bat
%wrapper_bat% -i %conf_file%
pause


