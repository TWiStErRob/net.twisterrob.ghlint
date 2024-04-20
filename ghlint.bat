@echo off
echo Run `gradlew cliJar` before using this.
java -jar "%~dp0\modules\ghlint-cli\build\cli\ghlint.jar" %*
