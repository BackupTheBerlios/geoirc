@echo off
rem Author netseeker aka michael manske
rem 2003-10-05
rem Modifications made by Pistos (Alex Reyes)
rem 2003-10-15

if "%OS%"=="Windows_NT" @setlocal

set _JAVACMD=javaw.exe
set LOCALCLASSPATH=%CLASSPATH%;jar\geoirc.jar;jar\jdom.jar;jar\jython.jar;jar\skinlf.jar;jar\jacl.jar;jar\tcljava.jar
rem for %%i in ("jar\*.jar") do call "append.bat" %%i

if "%JAVA_HOME%" == "" goto no_java_detected
if not exist "%JAVA_HOME%\bin\javaw.exe" goto no_java_detected
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\javaw.exe
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
echo %_JAVACMD% -cp "%LOCALCLASSPATH%" geoirc.GeoIRC
%_JAVACMD% -cp "%LOCALCLASSPATH%" geoirc.GeoIRC
goto end


:end
set LOCALCLASSPATH=
set _JAVACMD=

if "%OS%"=="Windows_NT" @endlocal
exit