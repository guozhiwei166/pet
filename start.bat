@echo off
chcp 936 >nul
title MaoGouJiYang YiJian QiDong
cd /d "%~dp0"
echo ============================================================
echo   MaoGouJiYang YiJian QiDong
echo ============================================================
echo.

REM ---------- 0. Lu Jing Pei Zhi ----------
set "REDIS_EXE=C:\tools\redis\redis-server.exe"
set "NODE_DIR=C:\tools\nodejs\node-v22.17.0-win-x64"
set "JAVA_HOME=C:\tools\jdk17\jdk-17.0.20.1+1"
set "MAVEN_CMD=C:\Users\guo'zhi'wei\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd"

REM ---------- 1. Jian Cha MySQL ----------
echo [1/4] Jian Cha MySQL...
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
  echo        MySQL OK
) else (
  echo        [!!] MySQL WEI YUN XING! Qing Xian Qi Dong MySQL Fu Wu.
  echo        Can Kao README-启动说明.md
  start "" "%~dp0README-启动说明.md"
  pause
  exit /b 1
)

REM ---------- 2. Qi Dong Redis ----------
echo [2/4] Qi Dong Redis...
netstat -ano | findstr ":6379" | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
  echo        Redis OK
) else (
  start "MaoGouJiYang-Redis" "%REDIS_EXE%" --port 6379 --save "" --appendonly no
  echo        Redis Qi Dong Zhong...
)

REM ---------- 3. Qi Dong Hou Duan ----------
echo [3/4] Qi Dong Hou Duan (http://localhost:8082)...
netstat -ano | findstr ":8082" | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
  echo        Hou Duan OK
) else (
  start "MaoGouJiYang-Backend" cmd /k "set JAVA_HOME=%JAVA_HOME%&& set MAVEN_OPTS=-Dfile.encoding=UTF-8&& cd /d %~dp0petcare-backend&& "%MAVEN_CMD%" spring-boot:run"
  echo        Hou Duan Qi Dong Zhong...
)

REM ---------- 4. Qi Dong Qian Duan ----------
echo [4/4] Qi Dong Qian Duan (http://localhost:5190)...
netstat -ano | findstr ":5190" | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
  echo        Qian Duan OK
) else (
  start "MaoGouJiYang-Frontend" cmd /k "set PATH=%NODE_DIR%;%%PATH%%&& cd /d %~dp0petcare-frontend&& npm run dev"
  echo        Qian Duan Qi Dong Zhong...
)

echo.
echo ============================================================
echo   Qi Dong Wan Cheng!
echo   Wang Zhi:  http://localhost:5190
echo   Zhang Hao:  user/user123  staff/staff123  admin/admin123
echo   Ting Zhi:  Shuang Ji stop.bat
echo ============================================================
echo.
echo Deng Dai Hou Duan Jiu Xu, Zi Dong Da Kai Liu Lan Qi...
set /a tries=0
:waitloop
netstat -ano | findstr ":8082" | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
  ping -n 4 127.0.0.1 >nul
  start http://localhost:5190
  goto done
)
set /a tries+=1
if %tries% geq 30 goto done
ping -n 3 127.0.0.1 >nul
goto waitloop
:done
echo Ben Chuang Kou Ke Guan Bi.
ping -n 6 127.0.0.1 >nul
