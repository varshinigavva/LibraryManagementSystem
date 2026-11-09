@echo off
if not exist bin mkdir bin
javac -d bin src\com\library\*.java src\com\library\model\*.java src\com\library\storage\*.java src\com\library\service\*.java src\com\library\ui\*.java test\*.java
echo Build complete.
