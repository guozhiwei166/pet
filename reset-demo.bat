@echo off
chcp 936 >nul
title MaoGouJiYang Reset Demo Data
echo ============================================================
echo   MaoGouJiYang Reset Demo Data
echo   This will REBUILD the database with fresh demo data.
echo   Current data will be LOST.
echo ============================================================
echo.
set /p ok=Type YES to continue: 
if /i not "%ok%"=="YES" (
  echo Cancelled.
  pause
  exit /b 0
)

echo Stopping backend first (port 8082)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8082" ^| findstr "LISTENING"') do (
  taskkill /f /pid %%p >nul 2>&1 && echo   stopped PID %%p
)

echo.
echo Starting backend with demo profile to rebuild database...
echo After it starts (Started PetCareApplication), press Ctrl+C to stop,
echo then run start.bat to use the fresh data.
echo.
cd /d "%~dp0petcare-backend"
set "JAVA_HOME=C:\tools\jdk17\jdk-17.0.20.1+1"
set "MAVEN_OPTS=-Dfile.encoding=UTF-8"
"C:\Users\guo'zhi'wei\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=demo
pause
