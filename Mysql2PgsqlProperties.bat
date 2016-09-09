@echo off
pushd %~dp0
:: get the variable
set command=
:: build the command by shitfting attribute.
:LABEL
if [%1]==[]  GOTO EXIT
set command=%command% %1
shift
GOTO LABEL
:EXIT
echo command = %command%
:: run the programm to transfer the data.

:: We have to slip into two command, otherwise the command is too long.
:: first we have to change properties file.
java -Dfile.encoding=utf-8 -jar Mysql2Pgsql-1.0.0.jar %command%
:: check the error code of the java programm, the programm return 0 if it succeed.
IF %ERRORLEVEL% NEQ 0 EXIT /B 2

popd