@echo off
setlocal
cd /d "%~dp0"
call mvn "-Dmaven.repo.local=.maven-repository" javafx:run
if errorlevel 1 (
    echo.
    echo EventProof could not start or exited with an error.
    echo Check the Maven output above, Java and Maven installation, and local database setup.
    pause
    exit /b 1
)
endlocal
