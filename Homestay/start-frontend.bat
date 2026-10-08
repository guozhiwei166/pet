@echo off
chcp 65001 >nul
title 云栖民宿 - 前端启动器
echo ========================================
echo   云栖民宿 前端启动
echo   启动后访问 http://localhost:5173
echo   关闭本窗口 = 停止前端
echo ========================================
echo.
cd /d "%~dp0frontend"

REM 检查 node_modules 是否存在,不存在则安装
if not exist node_modules (
    echo 首次运行,正在安装依赖,请耐心等待...
    call npm install
)

echo 正在启动前端开发服务器...
echo.
call npm run dev

pause
