@echo off
setlocal EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "WRAPPER_PROPERTIES=%SCRIPT_DIR%.mvn\wrapper\maven-wrapper.properties"

for /f "tokens=1,* delims==" %%A in ('findstr /i "distributionUrl" "%WRAPPER_PROPERTIES%"') do set "DISTRIBUTION_URL=%%B"
for %%F in (%DISTRIBUTION_URL%) do set "DISTRIBUTION_FILENAME=%%~nxF"
set "DISTRIBUTION_NAME=%DISTRIBUTION_FILENAME:.zip=%"

if not defined MAVEN_USER_HOME set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "CACHE_DIR=%MAVEN_USER_HOME%\wrapper\dists\%DISTRIBUTION_NAME%"

if not exist "%CACHE_DIR%\bin\mvn.cmd" (
  echo [mvnw] Downloading %DISTRIBUTION_URL%
  md "%CACHE_DIR%" 2>nul
  powershell -NoProfile -Command "Invoke-WebRequest '%DISTRIBUTION_URL%' -OutFile '%MAVEN_USER_HOME%\wrapper\dists\%DISTRIBUTION_FILENAME%'"
  powershell -NoProfile -Command "Expand-Archive '%MAVEN_USER_HOME%\wrapper\dists\%DISTRIBUTION_FILENAME%' -DestinationPath '%MAVEN_USER_HOME%\wrapper\dists' -Force"
  del "%MAVEN_USER_HOME%\wrapper\dists\%DISTRIBUTION_FILENAME%"
)

"%CACHE_DIR%\bin\mvn.cmd" %*
