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

setlocal enabledelayedexpansion
echo command = %command%
:: run the programm to transfer the data and print error code in a communication pipe, we need to open a son process for encoding history terminal by default is not in utf-8
start /w java -Dfile.encoding=utf-8 -jar Mysql2Pgsql-1.0.0.jar %command%

echo %errorlevel% > fichier.txt


popd