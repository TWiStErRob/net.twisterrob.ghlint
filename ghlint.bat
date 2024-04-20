@echo off
setlocal

set jar=%~dp0modules\ghlint-cli\build\cli\ghlint.jar
echo Run `gradlew cliJar` before using "%jar%".

rem set jar=%~dp0modules\ghlint-cli\build\libs\ghlint-cli-0.4.2-SNAPSHOT-fat.jar
rem echo Run `gradlew fatJar` before using "%jar%".

java -jar "%jar%" %*
