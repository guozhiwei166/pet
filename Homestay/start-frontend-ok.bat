@echo off
title Homestay Frontend
cd /d C:\Users\L.L\Desktop\Homestay\frontend
if not exist node_modules (
  echo installing dependencies...
  call npm install
)
echo starting frontend at http://localhost:5173
call npm run dev
pause