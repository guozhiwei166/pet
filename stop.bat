@echo off
chcp 936 >nul
title MaoGouJiYang Stop
echo ============================================================
echo   MaoGouJiYang Stop (ports 5190 / 8082 / 6379)
echo ============================================================
echo.

echo [1/3] Stop Frontend (5190)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5190" ^| findstr "LISTENING"') do (
  taskkill /f /pid %%p >nul 2>&1 && echo        stopped PID %%p
)

echo [2/3] Stop Backend (8082)...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8082" ^| findstr "LISTENING"') do (
  taskkill /f /pid %%p >nul 2>&1 && echo        stopped PID %%p
)

echo [3/3] Stop Redis (6379)?
set /p ans=Stop Redis too? (y/N):
if /i "%ans%"=="y" (
  for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":6379" ^| findstr "LISTENING"') do (
    taskkill /f /pid %%p >nul 2>&1 && echo        stopped PID %%p
  )
) else (
  echo        skip (Redis stays, faster next start)
)

echo.
echo Done. You may close the black windows.
timeout /t 3 >nul
