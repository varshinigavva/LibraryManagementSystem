@echo off
call build.bat
jar cfe LibraryManagementSystem.jar com.library.Main -C bin .
echo.
echo ============================================================
echo   Executable JAR created: LibraryManagementSystem.jar
echo   You can run it with: java -jar LibraryManagementSystem.jar
echo   or simply double-click LibraryManagementSystem.jar in Windows!
echo ============================================================
