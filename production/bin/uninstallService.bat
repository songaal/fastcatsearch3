cd %~dp0\..\service\windows_yajsw
call setenv.bat
%wrapper_bat% -r %conf_file%
pause