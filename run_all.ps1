# =========================================================
# FinOps Cloud Intelligence Platform - Quick Start Script
# =========================================================

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host " Starting FinOps Cloud Intelligence Full Stack... " -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

$baseDir = "D:\FinOps\files-mentioned-by-the-user-ai"
$mavenPath = "$baseDir\work\tools\apache-maven-3.9.9\bin\mvn.cmd"

# 1. Start Backend in a separate PowerShell window
Write-Host "`n[1/2] Launching Spring Boot Backend (Port 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\backend'; Write-Host 'Starting FinOps Backend on http://localhost:8080...' -ForegroundColor Green; & '$mavenPath' spring-boot:run -Pdatabase '-Dspring-boot.run.profiles=database'"

# 2. Start Frontend in a separate PowerShell window
Write-Host "[2/2] Launching React + Vite Frontend (Port 5173)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\frontend'; Write-Host 'Starting FinOps Frontend on http://localhost:5173...' -ForegroundColor Green; npm run dev"

Start-Sleep -Seconds 3

Write-Host "`n===================================================" -ForegroundColor Green
Write-Host " Both servers are starting up!" -ForegroundColor Green
Write-Host " Backend API:  http://localhost:8080" -ForegroundColor Cyan
Write-Host " Frontend Web: http://localhost:5173" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Green
