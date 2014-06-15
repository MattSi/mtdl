@echo off
set TPCLASS=.\lib\commons-net-3.3.jar

java -Xbootclasspath/a:%TPCLASS% -jar .\bin\mtdl.jar %*
