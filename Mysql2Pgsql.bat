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

setlocal enabledelayedexpansion
:: then we run the programm as administrator. 
java -Dfile.encoding=utf-8 -jar Mysql2Pgsql-1.0.0.jar %command%
:: check the error code, where the process java write the value of the error code, file fichier.txt is a kind of communication pipe, I don't suceed to do otherwise.

IF %errorlevel% NEQ 0 (
  :: if we don't succeed to save we exit.
  IF %errorlevel%==2 (EXIT /B  1)
  :: we return 0, in order to continue the execution to kepp a stable environnement.
  EXIT /B  0
)
popd