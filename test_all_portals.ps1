$baseUrl = "http://localhost:8085"
$ErrorActionPreference = "Stop"

function Assert-Step($title, $condition, $details) {
    if ($condition) {
        Write-Host " [PASS] $title - $details" -ForegroundColor Green
        return $true
    } else {
        Write-Host " [FAIL] $title - $details" -ForegroundColor Red
        return $false
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "HIRESMART ATS - ROLE-BASED ACCESS CONTROL (RBAC) & PORTAL TESTS" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# -------------------------------------------------------------
# 1. UNAUTHENTICATED & FORGED TOKEN SECURITY TESTS
# -------------------------------------------------------------
Write-Host "`n>>> SECTION 1: UNAUTHENTICATED & FORGED CREDENTIAL SECURITY..." -ForegroundColor Yellow

# 1.1 Unauthenticated access to admin endpoints must return 401
try {
    $unauthAdmin = Invoke-WebRequest -Uri "$baseUrl/api/admin/users" -Method Get -UseBasicParsing
    Assert-Step "1.1 Unauthenticated Admin Access" ($unauthAdmin.StatusCode -eq 401) "Got status: $($unauthAdmin.StatusCode)"
} catch {
    $is401 = ($_.Exception.Message -match "401" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Unauthorized)
    Assert-Step "1.1 Unauthenticated Admin Access" $is401 "Correctly blocked with HTTP 401 Unauthorized"
}

# 1.2 Unauthenticated access to recruiter applications must return 401
try {
    $unauthApps = Invoke-WebRequest -Uri "$baseUrl/api/applications" -Method Get -UseBasicParsing
    Assert-Step "1.2 Unauthenticated Applications Access" ($unauthApps.StatusCode -eq 401) "Got status: $($unauthApps.StatusCode)"
} catch {
    $is401 = ($_.Exception.Message -match "401" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Unauthorized)
    Assert-Step "1.2 Unauthenticated Applications Access" $is401 "Correctly blocked with HTTP 401 Unauthorized"
}

# 1.3 Forged / invalid token must return 401
try {
    $forgedRes = Invoke-WebRequest -Uri "$baseUrl/api/applications" -Method Get -Headers @{ "Authorization" = "Bearer invalid_forged_token_xyz" } -UseBasicParsing
    Assert-Step "1.3 Forged Token Rejection" ($forgedRes.StatusCode -eq 401) "Got status: $($forgedRes.StatusCode)"
} catch {
    $is401 = ($_.Exception.Message -match "401" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Unauthorized)
    Assert-Step "1.3 Forged Token Rejection" $is401 "Correctly rejected with HTTP 401 Unauthorized"
}

# 1.4 Invalid credentials login rejected
$badLogin = @{
    email = "candidate@hiresmart.com"
    password = "wrong_password_999"
} | ConvertTo-Json

try {
    $badRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $badLogin -ContentType "application/json"
    Assert-Step "1.4 Bad Password Login" ($badRes.success -eq $false) "Rejected: $($badRes.message)"
} catch {
    Assert-Step "1.4 Bad Password Login" $true "Rejected with HTTP error"
}

# 1.5 Block public admin registration
$adminReg = @{
    fullName = "Malicious Actor"
    email = "hacker@domain.com"
    password = "password123"
    role = "ADMIN"
} | ConvertTo-Json

try {
    $adminRegRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $adminReg -ContentType "application/json"
    Assert-Step "1.5 Block Public Admin Registration" ($adminRegRes.success -eq $false) "Correctly blocked: $($adminRegRes.message)"
} catch {
    Assert-Step "1.5 Block Public Admin Registration" $true "Rejected with HTTP error"
}

# -------------------------------------------------------------
# 2. STUDENT PORTAL & AUTHORIZATION TESTS
# -------------------------------------------------------------
Write-Host "`n>>> SECTION 2: STUDENT PORTAL & AUTHORIZATION TESTS..." -ForegroundColor Yellow

# 2.1 Student Registration
$candEmail = "student.test.$(Get-Random)@hiresmart.com"
$candRegPayload = @{
    fullName = "Kavita Rao"
    email = $candEmail
    password = "password123"
    role = "CANDIDATE"
    phone = "+91 9876543210"
    headline = "Backend Java & Cloud Engineer"
    location = "Bengaluru, India"
} | ConvertTo-Json

try {
    $candRegRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $candRegPayload -ContentType "application/json"
    Assert-Step "2.1 Student Registration" ($candRegRes.success -eq $true -and $candRegRes.user.role -eq "CANDIDATE") "Student account created with ID $($candRegRes.user.id)"
} catch {
    Assert-Step "2.1 Student Registration" $false $_.Exception.Message
}

# 2.2 Student Login (Valid Credentials)
$loginPayload = @{
    email = "candidate@hiresmart.com"
    password = "password123"
    expectedRole = "CANDIDATE"
} | ConvertTo-Json

try {
    $candLoginRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginPayload -ContentType "application/json"
    $candToken = $candLoginRes.token
    $candId = if ($candLoginRes.candidateProfile) { [long]$candLoginRes.candidateProfile.id } elseif ($candLoginRes.profile) { [long]$candLoginRes.profile.id } else { 1 }
    $candHeaders = @{ "Authorization" = "Bearer $candToken" }
    Assert-Step "2.2 Student Login" ($candLoginRes.success -eq $true -and $candLoginRes.user.role -eq "CANDIDATE" -and $candToken.Length -gt 10) "Authenticated as CANDIDATE with token"
} catch {
    Assert-Step "2.2 Student Login" $false $_.Exception.Message
}

# 2.3 Server-side Token Session Validation
try {
    $valRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/validate" -Method Get -Headers $candHeaders
    Assert-Step "2.3 Student Session Validation" ($valRes.valid -eq $true -and $valRes.user.role -eq "CANDIDATE") "Server verified session as CANDIDATE"
} catch {
    Assert-Step "2.3 Student Session Validation" $false $_.Exception.Message
}

# 2.4 Student Dashboard: Profile retrieval & update
try {
    $profile = Invoke-RestMethod -Uri "$baseUrl/api/candidates/profile/$candId" -Method Get -Headers $candHeaders
    $profile.headline = "Lead Cloud & Microservices Architect"
    $upJson = $profile | ConvertTo-Json
    $upRes = Invoke-RestMethod -Uri "$baseUrl/api/candidates/profile/$candId" -Method Put -Body $upJson -ContentType "application/json" -Headers $candHeaders
    Assert-Step "2.4 Student Profile Access & Update" ($upRes -match "updated") "Updated student profile #$candId"
} catch {
    Assert-Step "2.4 Student Profile Access & Update" $false $_.Exception.Message
}

# 2.5 Student Dashboard: Resumes & Applications
try {
    $candResumes = Invoke-RestMethod -Uri "$baseUrl/api/resumes/candidate/$candId" -Method Get -Headers $candHeaders
    $candApps = Invoke-RestMethod -Uri "$baseUrl/api/applications/candidate/$candId" -Method Get -Headers $candHeaders
    Assert-Step "2.5 Student Resumes & Applications Access" ($candResumes.Count -ge 1) "Resumes found: $($candResumes.Count), Applications: $($candApps.Count)"
} catch {
    Assert-Step "2.5 Student Resumes & Applications Access" $false $_.Exception.Message
}

# 2.6 Student Dashboard: Mock Interview Studio
try {
    $questions = Invoke-RestMethod -Uri "$baseUrl/api/mock-interview/questions" -Method Get -Headers $candHeaders
    $firstQ = ($questions | Select-Object -First 1)
    $ansPayload = @{
        questionId = [long]$firstQ.id
        candidateAnswer = "Spring Boot optimizes dependency management through starter POMs and provides conditional auto-configuration."
    } | ConvertTo-Json
    $evalRes = Invoke-RestMethod -Uri "$baseUrl/api/mock-interview/submit" -Method Post -Body $ansPayload -ContentType "application/json" -Headers $candHeaders
    Assert-Step "2.6 Student AI Mock Interview" ($evalRes.score -gt 0) "Evaluated answer with score: $($evalRes.score)/100"
} catch {
    Assert-Step "2.6 Student AI Mock Interview" $false $_.Exception.Message
}

# 2.7 SECURITY CHECK: Student BLOCKED from Recruiter Job Creation API (403 Forbidden)
$illegalJob = @{
    recruiterId = 1
    title = "Unauthorized Student Job Post"
    companyName = "Illegal Corp"
} | ConvertTo-Json

try {
    $jobRes = Invoke-WebRequest -Uri "$baseUrl/api/jobs" -Method Post -Body $illegalJob -ContentType "application/json" -Headers $candHeaders -UseBasicParsing
    Assert-Step "2.7 Student Blocked from Job Creation" ($jobRes.StatusCode -eq 403) "Unexpected status: $($jobRes.StatusCode)"
} catch {
    $is403 = ($_.Exception.Message -match "403" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Forbidden)
    Assert-Step "2.7 Student Blocked from Job Creation" $is403 "Blocked with HTTP 403 Forbidden"
}

# 2.8 SECURITY CHECK: Student BLOCKED from viewing All Pipeline Applications (403 Forbidden)
try {
    $pipeRes = Invoke-WebRequest -Uri "$baseUrl/api/applications" -Method Get -Headers $candHeaders -UseBasicParsing
    Assert-Step "2.8 Student Blocked from Recruiter Pipeline" ($pipeRes.StatusCode -eq 403) "Unexpected status: $($pipeRes.StatusCode)"
} catch {
    $is403 = ($_.Exception.Message -match "403" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Forbidden)
    Assert-Step "2.8 Student Blocked from Recruiter Pipeline" $is403 "Blocked with HTTP 403 Forbidden"
}

# 2.9 SECURITY CHECK: Student BLOCKED from Admin User Management API (403 Forbidden)
try {
    $adminRes = Invoke-WebRequest -Uri "$baseUrl/api/admin/users" -Method Get -Headers $candHeaders -UseBasicParsing
    Assert-Step "2.9 Student Blocked from Admin Users API" ($adminRes.StatusCode -eq 403) "Unexpected status: $($adminRes.StatusCode)"
} catch {
    $is403 = ($_.Exception.Message -match "403" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Forbidden)
    Assert-Step "2.9 Student Blocked from Admin Users API" $is403 "Blocked with HTTP 403 Forbidden"
}

# 2.10 SECURITY CHECK: Student BLOCKED from accessing another Candidate's private applications (403 Forbidden)
try {
    $foreignApps = Invoke-WebRequest -Uri "$baseUrl/api/applications/candidate/9999" -Method Get -Headers $candHeaders -UseBasicParsing
    Assert-Step "2.10 Student Isolated from Other Candidates' Applications" ($foreignApps.StatusCode -eq 403) "Unexpected status: $($foreignApps.StatusCode)"
} catch {
    $is403 = ($_.Exception.Message -match "403" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Forbidden)
    Assert-Step "2.10 Student Isolated from Other Candidates' Applications" $is403 "Blocked with HTTP 403 Forbidden"
}

# -------------------------------------------------------------
# 3. RECRUITER PORTAL & AUTHORIZATION TESTS
# -------------------------------------------------------------
Write-Host "`n>>> SECTION 3: RECRUITER PORTAL & AUTHORIZATION TESTS..." -ForegroundColor Yellow

# 3.1 Recruiter Login
$recLogin = @{
    email = "recruiter@hiresmart.com"
    password = "password123"
    expectedRole = "RECRUITER"
} | ConvertTo-Json

try {
    $recRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $recLogin -ContentType "application/json"
    $recToken = $recRes.token
    $recHeaders = @{ "Authorization" = "Bearer $recToken" }
    $recUserId = [long]$recRes.user.id
    Assert-Step "3.1 Recruiter Login" ($recRes.success -eq $true -and $recRes.user.role -eq "RECRUITER") "Authenticated as RECRUITER"
} catch {
    Assert-Step "3.1 Recruiter Login" $false $_.Exception.Message
}

# 3.2 Recruiter Session Validation
try {
    $valRec = Invoke-RestMethod -Uri "$baseUrl/api/auth/validate" -Method Get -Headers $recHeaders
    Assert-Step "3.2 Recruiter Session Validation" ($valRec.valid -eq $true -and $valRec.user.role -eq "RECRUITER") "Server verified session as RECRUITER"
} catch {
    Assert-Step "3.2 Recruiter Session Validation" $false $_.Exception.Message
}

# 3.3 Recruiter Job Creation
$newJob = @{
    recruiterId = $recUserId
    title = "Principal Distributed Systems Engineer $(Get-Random)"
    companyName = "Apex Intelligence Labs"
    department = "Core Infrastructure"
    location = "Bengaluru / Remote"
    jobType = "Full-time"
    experienceRequired = 6
    qualificationRequired = "B.Tech / M.Tech in CS/IT"
    requiredSkills = "Java, Spring Boot, Kafka, Kubernetes, MySQL, Distributed Systems"
    description = "Design and scale high throughput streaming systems."
    status = "ACTIVE"
} | ConvertTo-Json

try {
    $createdJob = Invoke-RestMethod -Uri "$baseUrl/api/jobs" -Method Post -Body $newJob -ContentType "application/json" -Headers $recHeaders
    $newJobId = [long]$createdJob.id
    Assert-Step "3.3 Recruiter Job Creation" ($newJobId -gt 0) "Job #$newJobId created successfully"
} catch {
    Assert-Step "3.3 Recruiter Job Creation" $false $_.Exception.Message
}

# 3.4 Recruiter Pipeline Applications Review
try {
    $allApps = Invoke-RestMethod -Uri "$baseUrl/api/applications" -Method Get -Headers $recHeaders
    $targetApp = ($allApps | Select-Object -First 1)
    $targetAppId = [long]$targetApp.id
    Assert-Step "3.4 Recruiter View Applications Pipeline" ($allApps.Count -ge 1) "Total applications reviewed: $($allApps.Count)"
} catch {
    Assert-Step "3.4 Recruiter View Applications Pipeline" $false $_.Exception.Message
}

# 3.5 Recruiter Update Application Status
$statusUpdate = @{
    newStatus = "SHORTLISTED"
    changeReason = "Strong expertise in distributed systems."
    recruiterNotes = "Candidate successfully screened and shortlisted."
    changedByUserId = $recUserId
} | ConvertTo-Json

try {
    $upStatus = Invoke-RestMethod -Uri "$baseUrl/api/applications/$targetAppId/status" -Method Put -Body $statusUpdate -ContentType "application/json" -Headers $recHeaders
    Assert-Step "3.5 Recruiter Update Status" ($upStatus -match "updated") "Application #$targetAppId updated to SHORTLISTED"
} catch {
    Assert-Step "3.5 Recruiter Update Status" $false $_.Exception.Message
}

# 3.6 Recruiter Schedule Interview
$schedPayload = @{
    applicationId = $targetAppId
    roundType = "TECHNICAL"
    scheduledTime = (Get-Date).AddDays(3).ToString("yyyy-MM-ddTHH:mm:ss")
    interviewerName = "Sarah Jenkins"
    interviewerEmail = "recruiter@hiresmart.com"
    meetingLink = "https://meet.google.com/hsm-tech-interview"
} | ConvertTo-Json

try {
    $schedRes = Invoke-RestMethod -Uri "$baseUrl/api/interviews/schedule" -Method Post -Body $schedPayload -ContentType "application/json" -Headers $recHeaders
    $ivId = [long]$schedRes.id
    Assert-Step "3.6 Recruiter Schedule Interview" ($ivId -gt 0) "Scheduled Interview #$ivId at $($schedRes.meetingLink)"
} catch {
    Assert-Step "3.6 Recruiter Schedule Interview" $false $_.Exception.Message
}

# 3.7 Recruiter Submit Interview Feedback
$fbPayload = @{
    technicalScore = 9
    communicationScore = 9
    problemSolvingScore = 9
    feedbackNotes = "Exceptional system design and problem solving skills."
    recommendation = "HIRE"
    status = "COMPLETED"
} | ConvertTo-Json

try {
    $fbRes = Invoke-RestMethod -Uri "$baseUrl/api/interviews/$ivId/feedback" -Method Post -Body $fbPayload -ContentType "application/json" -Headers $recHeaders
    Assert-Step "3.7 Recruiter Interview Evaluation" ($fbRes -match "submitted") "Feedback submitted with recommendation HIRE"
} catch {
    Assert-Step "3.7 Recruiter Interview Evaluation" $false $_.Exception.Message
}

# 3.8 SECURITY CHECK: Recruiter BLOCKED from Admin Users API (403 Forbidden)
try {
    $recAdmin = Invoke-WebRequest -Uri "$baseUrl/api/admin/users" -Method Get -Headers $recHeaders -UseBasicParsing
    Assert-Step "3.8 Recruiter Blocked from Admin Users API" ($recAdmin.StatusCode -eq 403) "Unexpected status: $($recAdmin.StatusCode)"
} catch {
    $is403 = ($_.Exception.Message -match "403" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Forbidden)
    Assert-Step "3.8 Recruiter Blocked from Admin Users API" $is403 "Blocked with HTTP 403 Forbidden"
}

# 3.9 SECURITY CHECK: Recruiter BLOCKED from modifying Student Personal Profile (403 Forbidden)
$tamperProfile = @{
    fullName = "Tampered Name"
    skills = "Hacked"
} | ConvertTo-Json

try {
    $tamperRes = Invoke-WebRequest -Uri "$baseUrl/api/candidates/profile/1" -Method Put -Body $tamperProfile -ContentType "application/json" -Headers $recHeaders -UseBasicParsing
    Assert-Step "3.9 Recruiter Blocked from Modifying Candidate Profile" ($tamperRes.StatusCode -eq 403) "Unexpected status: $($tamperRes.StatusCode)"
} catch {
    $is403 = ($_.Exception.Message -match "403" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Forbidden)
    Assert-Step "3.9 Recruiter Blocked from Modifying Candidate Profile" $is403 "Blocked with HTTP 403 Forbidden"
}

# -------------------------------------------------------------
# 4. ADMIN PORTAL & AUTHORIZATION TESTS
# -------------------------------------------------------------
Write-Host "`n>>> SECTION 4: ADMIN PORTAL & AUTHORIZATION TESTS..." -ForegroundColor Yellow

# 4.1 Admin Login
$adminLogin = @{
    email = "admin@hiresmart.com"
    password = "admin123"
    expectedRole = "ADMIN"
} | ConvertTo-Json

try {
    $adminRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $adminLogin -ContentType "application/json"
    $adminToken = $adminRes.token
    $adminHeaders = @{ "Authorization" = "Bearer $adminToken" }
    Assert-Step "4.1 Admin Login" ($adminRes.success -eq $true -and $adminRes.user.role -eq "ADMIN") "Authenticated as ADMIN"
} catch {
    Assert-Step "4.1 Admin Login" $false $_.Exception.Message
}

# 4.2 Admin Session Validation
try {
    $valAdmin = Invoke-RestMethod -Uri "$baseUrl/api/auth/validate" -Method Get -Headers $adminHeaders
    Assert-Step "4.2 Admin Session Validation" ($valAdmin.valid -eq $true -and $valAdmin.user.role -eq "ADMIN") "Server verified session as ADMIN"
} catch {
    Assert-Step "4.2 Admin Session Validation" $false $_.Exception.Message
}

# 4.3 Admin User Management Directory
try {
    $users = Invoke-RestMethod -Uri "$baseUrl/api/admin/users" -Method Get -Headers $adminHeaders
    Assert-Step "4.3 Admin User Management Directory" ($users.Count -ge 3) "Retrieved $($users.Count) user records"
} catch {
    Assert-Step "4.3 Admin User Management Directory" $false $_.Exception.Message
}

# 4.4 Admin Audit Logs
try {
    $auditLogs = Invoke-RestMethod -Uri "$baseUrl/api/admin/audit-logs" -Method Get -Headers $adminHeaders
    Assert-Step "4.4 Admin Security Audit Logs" ($auditLogs.Count -ge 1) "Retrieved $($auditLogs.Count) security audit logs"
} catch {
    Assert-Step "4.4 Admin Security Audit Logs" $false $_.Exception.Message
}

# 4.5 Admin Toggle User Status
try {
    $toggleRes = Invoke-RestMethod -Uri "$baseUrl/api/admin/users/1/status?active=false" -Method Put -Headers $adminHeaders
    Assert-Step "4.5 Admin Toggle User Status" ($toggleRes.message -match "updated") "Admin successfully updated user status: $($toggleRes.message)"
    # Toggle back to ensure candidate is active
    Invoke-RestMethod -Uri "$baseUrl/api/admin/users/1/status?active=true" -Method Put -Headers $adminHeaders | Out-Null
} catch {
    Assert-Step "4.5 Admin Toggle User Status" $false $_.Exception.Message
}

# -------------------------------------------------------------
# 5. LOGOUT & SESSION TERMINATION TESTS
# -------------------------------------------------------------
Write-Host "`n>>> SECTION 5: LOGOUT & SESSION EXPIRATION TESTS..." -ForegroundColor Yellow

# 5.1 Logout Invalidation
try {
    $logoutRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/logout" -Method Post -Headers $candHeaders
    Assert-Step "5.1 Student Logout" ($logoutRes.success -eq $true) "Logout succeeded and invalidated token"
} catch {
    Assert-Step "5.1 Student Logout" $false $_.Exception.Message
}

# 5.2 Validate Terminated Token
try {
    $checkVal = Invoke-RestMethod -Uri "$baseUrl/api/auth/validate" -Method Get -Headers $candHeaders
    Assert-Step "5.2 Check Revoked Token on Validate" ($checkVal.valid -eq $false) "Session confirmed invalidated"
} catch {
    Assert-Step "5.2 Check Revoked Token on Validate" $true "Session validate rejected revoked token"
}

# 5.3 Attempting Protected Access with Revoked Token (Must return 401)
try {
    $revokedReq = Invoke-WebRequest -Uri "$baseUrl/api/stats" -Method Get -Headers $candHeaders -UseBasicParsing
    Assert-Step "5.3 Revoked Token Protected Resource Access" ($revokedReq.StatusCode -eq 401) "Unexpected status: $($revokedReq.StatusCode)"
} catch {
    $is401 = ($_.Exception.Message -match "401" -or $_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Unauthorized)
    Assert-Step "5.3 Revoked Token Protected Resource Access" $is401 "Correctly rejected with HTTP 401 Unauthorized"
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "ALL ROLE-BASED ACCESS & DASHBOARD VERIFICATION TESTS COMPLETED!" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
