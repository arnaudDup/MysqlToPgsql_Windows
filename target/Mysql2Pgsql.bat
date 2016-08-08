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

echo Changement de repertoire 
setlocal enabledelayedexpansion
:: then we run the programm as administrator. 
echo %~dp0resources
%~dp0resources\elevate -w -c runApplication %command%
:: check the error code, where the process java write the value of the error code, file fichier.txt is a kind of communication pipe, I don't suceed to do otherwise.
set /p VARIABLE=< fichier.txt

echo errorCode = %VARIABLE%
IF %VARIABLE% NEQ 0 (
  echo programm java fail for %command%
  :: we return 0, in order to continue the execution to kepp a stable environnement.
  EXIT /B  0
)
popd