@echo off
if "%1" == "" goto no_arg

set PKG_SRC_DIR=C:\GeoIRC
set PKG_OUTPUT_DIR=C:
set SOURCE_CODE_DIR=C:\Documents and Settings\Alex\My Documents\BerliOS\GeoIRC
set INFOZIP=C:\Program Files\InfoZip\zip.exe
set INNOSETUP=C:\Program Files\Inno Setup 4\iscc.exe

rem Backup the current settings
echo Backing up settings...
copy /y "%PKG_SRC_DIR%\settings.xml" C:\

echo Removing old CHANGELOGs...
del "%PKG_SRC_DIR%\CHANGELOG-*.txt"
rem Remove any previously generated package
echo Removing any previously generated %1 package...
del "%PKG_OUTPUT_DIR%\geoirc-%1.zip"
del "%PKG_OUTPUT_DIR%\*setup.exe"

echo Copying new CHANGELOG...
copy /y "%SOURCE_CODE_DIR%\CHANGELOG-%1.txt" "%PKG_SRC_DIR%"
echo Copying new settings.xml...
copy /y "%SOURCE_CODE_DIR%\settings.xml" "%PKG_SRC_DIR%"
echo Copying new geoirc.bat...
copy /y "%SOURCE_CODE_DIR%\installers\inno_setup\geoirc.bat" "%PKG_SRC_DIR%"
echo Copying new geoirc_silent.bat...
copy /y "%SOURCE_CODE_DIR%\installers\inno_setup\geoirc_silent.bat" "%PKG_SRC_DIR%"

echo Creating zip package...
"%INFOZIP%" -9 -D "%PKG_OUTPUT_DIR%\geoirc-%1.zip" "%PKG_SRC_DIR%\*.*" "%PKG_SRC_DIR%\jar\geoirc.jar" "%PKG_SRC_DIR%\sounds\*.*" "%PKG_SRC_DIR%\icons\*.*" "%PKG_SRC_DIR%\scripts\*.*" -x "%PKG_SRC_DIR%\cachedir" "%PKG_SRC_DIR%\logs" "%PKG_SRC_DIR%\themes" "%PKG_SRC_DIR%\jar"
echo Creating Windows setup package...
"%INNOSETUP%" "%SOURCE_CODE_DIR%\installers\inno_setup\GeoIRC.iss"
rename "%PKG_OUTPUT_DIR%\setup.exe" "GeoIRC %1 setup.exe"

rem Restore the backed up settings.
echo Restoring settings...
copy /y C:\settings.xml "%PKG_SRC_DIR%"

rem Copy to Linux machine for creation of tar.gz
echo Copying to Linux...
copy "%PKG_OUTPUT_DIR%\geoirc-%1.zip" K:\

goto end

:no_arg

echo.
echo Usage: mkgeoircpkg.bat [version string]
echo e.g. mkgeoircpkg.bat 0.3.4a
echo.

:end