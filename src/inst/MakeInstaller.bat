@echo off

set BRIDLE_HOME=%~dp0\..
set BRIDLE_VERSION=@BRIDLE_VERSION@
set BRIDLE_JAR=%BRIDLE_HOME%\BridleNSIS-%BRIDLE_VERSION%.jar

if "%NSIS_HOME%" == "" set NSIS_HOME=C:\Program Files (x86)\NSIS

java -jar "%BRIDLE_JAR%" -n "%NSIS_HOME%" -e Cp1252 installer.nsi "/DBRIDLE_VERSION=%BRIDLE_VERSION%" "/DBRIDLE_HOME=%BRIDLE_HOME%"
