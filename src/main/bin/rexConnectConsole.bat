@echo off

set work=%CD%

if [%work:~-3%]==[bin] cd ..

set JAVA_OPTIONS=-Xms32m -Xmx512m

:: Launch the application
java %JAVA_OPTIONS% %JAVA_ARGS% -cp .\lib\*; com.fabric.rexconnect.main.RexConnectConsole %*