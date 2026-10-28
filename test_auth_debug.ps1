$baseUrl = "http://localhost:8085"

Write-Host "=== TEST 1: CANDIDATE DEMO LOGIN ==="
$cLogin = @{
    email = "candidate@hiresmart.com"
    password = "password123"
    expectedRole = "CANDIDATE"
} | ConvertTo-Json
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $cLogin -ContentType "application/json"
    Write-Host "Candidate login success: $($res.success), message: $($res.message)" -ForegroundColor Green
} catch {
    Write-Host "Candidate login error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== TEST 2: RECRUITER DEMO LOGIN ==="
$rLogin = @{
    email = "recruiter@hiresmart.com"
    password = "password123"
    expectedRole = "RECRUITER"
} | ConvertTo-Json
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $rLogin -ContentType "application/json"
    Write-Host "Recruiter login success: $($res.success), message: $($res.message)" -ForegroundColor Green
} catch {
    Write-Host "Recruiter login error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== TEST 3: ADMIN DEMO LOGIN ==="
$aLogin = @{
    email = "admin@hiresmart.com"
    password = "admin123"
    expectedRole = "ADMIN"
} | ConvertTo-Json
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $aLogin -ContentType "application/json"
    Write-Host "Admin login success: $($res.success), message: $($res.message)" -ForegroundColor Green
} catch {
    Write-Host "Admin login error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== TEST 4: NEW CANDIDATE REGISTRATION ==="
$testEmail = "new.user.$(Get-Random)@example.com"
$newCand = @{
    fullName = "Test Student"
    email = $testEmail
    password = "password123"
    role = "CANDIDATE"
    phone = "+91 9876543210"
} | ConvertTo-Json
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $newCand -ContentType "application/json"
    Write-Host "New Candidate reg success: $($res.success), message: $($res.message)" -ForegroundColor Green
} catch {
    Write-Host "New Candidate reg error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== TEST 5: NEW RECRUITER REGISTRATION ==="
$testRecEmail = "new.recruiter.$(Get-Random)@example.com"
$newRec = @{
    fullName = "Test Recruiter"
    email = $testRecEmail
    password = "password123"
    role = "RECRUITER"
    companyName = "Acme Corp"
    phone = "+91 9876543210"
} | ConvertTo-Json
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $newRec -ContentType "application/json"
    Write-Host "New Recruiter reg success: $($res.success), message: $($res.message)" -ForegroundColor Green
} catch {
    Write-Host "New Recruiter reg error: $($_.Exception.Message)" -ForegroundColor Red
}
