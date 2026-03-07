# Fruit and Vegetable Sales System Quick Start Script
# Author: AI Assistant
# Date: 2026-03-08

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "       Fruit and Vegetable Sales System" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Set project paths
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $projectRoot "fruit-veg-backend"
$frontendPath = Join-Path $projectRoot "fruit-veg-frontend"
$mavenPath = Join-Path $projectRoot "apache-maven-3.8.8\bin\mvn.cmd"

# Check MySQL service status
Write-Host "[1/5] Checking MySQL service status..." -ForegroundColor Yellow
try {
    $mysqlService = Get-Service -Name MySQL* -ErrorAction SilentlyContinue
    if ($mysqlService -and $mysqlService.Status -eq 'Running') {
        Write-Host "SUCCESS - MySQL service is running" -ForegroundColor Green
    } else {
        Write-Host "ERROR - MySQL service is not running" -ForegroundColor Red
        Write-Host ""
        Write-Host "How to start MySQL service:" -ForegroundColor Yellow
        Write-Host "1. Open Service Manager (services.msc)" -ForegroundColor White
        Write-Host "2. Find MySQL service and start it" -ForegroundColor White
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
} catch {
    Write-Host "ERROR - Cannot check MySQL service status" -ForegroundColor Red
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""

# Check database
Write-Host "[2/5] Checking database..." -ForegroundColor Yellow
try {
    $env:MYSQL_PWD = "123456"
    $dbCheck = & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -e "USE fruit_veg_sales; SELECT 1;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "SUCCESS - Database fruit_veg_sales exists" -ForegroundColor Green
    } else {
        Write-Host "WARNING - Database does not exist, creating..." -ForegroundColor Yellow
        & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS fruit_veg_sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>&1 | Out-Null
        Write-Host "SUCCESS - Database created successfully" -ForegroundColor Green
    }
} catch {
    Write-Host "ERROR - Cannot connect to database" -ForegroundColor Red
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""

# Function to kill process using a specific port
function Stop-ProcessOnPort {
    param(
        [int]$Port
    )
    
    $portProcess = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | 
                    Where-Object { $_.State -eq 'Listen' } | 
                    Select-Object -ExpandProperty OwningProcess -ErrorAction SilentlyContinue
    
    if ($portProcess) {
        Write-Host "WARNING - Port ${Port} is in use by PID $($portProcess.Id), stopping..." -ForegroundColor Yellow
        try {
            Stop-Process -Id $portProcess.Id -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 2
            Write-Host "SUCCESS - Port ${Port} is now available" -ForegroundColor Green
        } catch {
            $errorMsg = $_.Exception.Message
            Write-Host "ERROR - Failed to stop process on port ${Port}: ${errorMsg}" -ForegroundColor Red
        }
    }
}

# Start backend service
Write-Host "[3/5] Starting backend service..." -ForegroundColor Yellow
Write-Host "Backend path: $backendPath" -ForegroundColor Gray
Write-Host "Maven path: $mavenPath" -ForegroundColor Gray

# Check and stop process on port 8080 if exists
Stop-ProcessOnPort -Port 8080

$backendLogPath = Join-Path $backendPath "backend.log"
if (Test-Path $backendLogPath) {
    Remove-Item $backendLogPath -Force
}

Write-Host "Start command: mvn spring-boot:run" -ForegroundColor Gray
Write-Host "Log file: $backendLogPath" -ForegroundColor Gray
Write-Host ""

$cmdBackend = "/c cd /d `"$backendPath`" && `"$mavenPath`" spring-boot:run > `"$backendLogPath`" 2>&1"
Start-Process -FilePath "cmd" -ArgumentList $cmdBackend -WindowStyle Normal

Write-Host "Waiting for backend service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Check if backend started successfully
$backendStarted = $false
$attempts = 0
$maxAttempts = 15

while ($attempts -lt $maxAttempts -and -not $backendStarted) {
    $attempts++
    Write-Host "Checking backend service... ($attempts/$maxAttempts)" -ForegroundColor Gray
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method Get -TimeoutSec 2 -UseBasicParsing -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 404 -or $response.StatusCode -eq 405) {
            $backendStarted = $true
            Write-Host "SUCCESS - Backend service started successfully!" -ForegroundColor Green
        }
    } catch {
        # Also check log file for startup message
        if (Test-Path $backendLogPath) {
            $logContent = Get-Content $backendLogPath -Tail 50 -ErrorAction SilentlyContinue
            if ($logContent -match "Started FruitVegBackendApplication") {
                $backendStarted = $true
                Write-Host "SUCCESS - Backend service started successfully!" -ForegroundColor Green
            }
        }
    }
    
    if (-not $backendStarted) {
        Start-Sleep -Seconds 2
    }
}

if (-not $backendStarted) {
    Write-Host "ERROR - Backend service failed to start, please check log: $backendLogPath" -ForegroundColor Red
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""

# Start frontend service
Write-Host "[4/5] Starting frontend service..." -ForegroundColor Yellow
Write-Host "Frontend path: $frontendPath" -ForegroundColor Gray

# Check and stop process on port 3000 if exists
Stop-ProcessOnPort -Port 3000

$frontendLogPath = Join-Path $frontendPath "frontend.log"
if (Test-Path $frontendLogPath) {
    Remove-Item $frontendLogPath -Force
}

Write-Host "Start command: npm run dev" -ForegroundColor Gray
Write-Host "Log file: $frontendLogPath" -ForegroundColor Gray
Write-Host ""

$cmdFrontend = "/c cd /d `"$frontendPath`" && npm run dev > `"$frontendLogPath`" 2>&1"
Start-Process -FilePath "cmd" -ArgumentList $cmdFrontend -WindowStyle Normal

Write-Host "Waiting for frontend service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Check if frontend started successfully
$frontendStarted = $false
$attempts = 0
$maxAttempts = 15

while ($attempts -lt $maxAttempts -and -not $frontendStarted) {
    $attempts++
    Write-Host "Checking frontend service... ($attempts/$maxAttempts)" -ForegroundColor Gray
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:3000" -Method Get -TimeoutSec 2 -UseBasicParsing -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $frontendStarted = $true
            Write-Host "SUCCESS - Frontend service started successfully!" -ForegroundColor Green
        }
    } catch {
        # Also check log file for startup message
        if (Test-Path $frontendLogPath) {
            $logContent = Get-Content $frontendLogPath -Tail 50 -ErrorAction SilentlyContinue
            if ($logContent -match "Local:" -and $logContent -match "http://localhost:3000") {
                $frontendStarted = $true
                Write-Host "SUCCESS - Frontend service started successfully!" -ForegroundColor Green
            }
        }
    }
    
    if (-not $frontendStarted) {
        Start-Sleep -Seconds 2
    }
}

if (-not $frontendStarted) {
    Write-Host "ERROR - Frontend service failed to start, please check log: $frontendLogPath" -ForegroundColor Red
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""

# Display startup success message
Write-Host "[5/5] Service startup completed!" -ForegroundColor Green
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "           Service startup successful!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Frontend Application:" -ForegroundColor White
Write-Host "  URL: http://localhost:3000" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend Service:" -ForegroundColor White
Write-Host "  URL: http://localhost:8080/api" -ForegroundColor Cyan
Write-Host "  API Docs: http://localhost:8080/doc.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "Log Files:" -ForegroundColor White
Write-Host "  Backend: $backendLogPath" -ForegroundColor Gray
Write-Host "  Frontend: $frontendLogPath" -ForegroundColor Gray
Write-Host ""
Write-Host "Tips:" -ForegroundColor Yellow
Write-Host "  - Press Ctrl+C to stop this script" -ForegroundColor White
Write-Host "  - Services will continue running in background" -ForegroundColor White
Write-Host "  - Closing command window will NOT stop services" -ForegroundColor White
Write-Host "  - To stop services, close the corresponding command windows manually" -ForegroundColor White
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Open browser to frontend application
Write-Host "Opening browser to frontend application..." -ForegroundColor Yellow
Start-Process "http://localhost:3000"
Write-Host ""

# Keep script running
Write-Host "Press Ctrl+C to exit script..." -ForegroundColor Gray
try {
    while ($true) {
        Start-Sleep -Seconds 1
    }
} catch [System.Management.Automation.PipelineStoppedException] {
    Write-Host ""
    Write-Host "Script stopped" -ForegroundColor Yellow
    Write-Host "Services are still running in background" -ForegroundColor Green
}
