@echo off
chcp 65001 >nul
echo ========================================
echo  云栖民宿 - 一键启动(后端 + 前端)
echo ========================================
echo.

echo [1/2] 启动后端 (http://localhost:8080) ...
start "backend" cmd /k "cd /d %~dp0backend && java -jar target\tourism-backend-1.0.0.jar"

echo [2/2] 启动前端 (http://localhost:5173) ...
start "frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo 两个窗口已启动:
echo   后端窗口标题 [backend]   -> http://localhost:8080
echo   前端窗口标题 [frontend]  -> http://localhost:5173
echo.
echo 浏览器打开 http://localhost:5173 即可访问
pause
