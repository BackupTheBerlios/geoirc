@echo off
rem Author netseeker aka michael manske
rem 10/05/2003

if "%OS%"=="Windows_NT" @setlocal

set _JAVACMD=java.exe
set LOCALCLASSPATH=%CLASSPATH%;lib
for %%i in ("lib\*.jar") do call "append.bat" %%i

if "%JAVA_HOME%" == "" goto no_java_detected
if not exist "%JAVA_HOME%\bin\java.exe" goto no_java_detected
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_HOME%\lib\rt.jar" set LOCALCLASSPATH=%JAVA_HOME%\lib\rt.jar;%LOCALCLASSPATH%
goto run_geoirc

:no_java_detected
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo If starting GeoIRC fails because the command "java" could not be found
echo you will need to set the JAVA_HOME environment variable
echo to the installation directory of java.
echo.


:run_geoirc
echo %_JAVACMD% -cp %LOCALCLASSPATH% geoirc.GeoIRC
%_JAVACMD% -cp %LOCALCLASSPATH% geoirc.GeoIRC
goto end


:end
set LOCALCLASSPATH=
set _JAVACMD=

if "%OS%"=="Windows_NT" @endlocal
