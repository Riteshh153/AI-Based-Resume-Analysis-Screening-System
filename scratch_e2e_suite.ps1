# HireSmart ATS - Comprehensive E2E Multi-User & Security Test Suite
$baseUrl = "http://localhost:8085"
$results = [System.Collections.Generic.List[PSObject]]::new()

function Record-Test {
    param(
        [string]$TestId,
        [string]$Category,
        [string]$Description,
        [bool]$Passed,
        [string]$Details = ""
    )
    $obj = [PSCustomObject]@{
        TestId      = $TestId
        Category    = $Category
        Description = $Description
        Passed      = $Passed
        Details     = $Details
    }
    $results.Add($obj)
    $status = if ($Passed) { "[PASS]" } else { "[FAIL]" }
    Write-Host "$status $TestId - $Description"
    if (-not $Passed -and $Details) {
        Write-Host "       Error: $Details" -ForegroundColor Red
    }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        $Body = $null
    )
    $fullUri = if ($Uri.StartsWith("http")) { $Uri } else { "$baseUrl$Uri" }
    $params = @{
        Method      = $Method
        Uri         = $fullUri
        Headers     = $Headers
        TimeoutSec  = 10
    }
    if ($null -ne $Body) {
        if ($Body -is [string]) {
            $params["Body"] = $Body
            if (-not $Headers.ContainsKey("Content-Type")) {
                $Headers["Content-Type"] = "application/json"
            }
        } else {
            $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
            if (-not $Headers.ContainsKey("Content-Type")) {
                $Headers["Content-Type"] = "application/json"
            }
        }
    }

    try {
        $resp = Invoke-WebRequest @params -UseBasicParsing -ErrorAction Stop
        $content = $resp.Content
        $json = $null
        try { $json = $content | ConvertFrom-Json } catch {}
        return @{
            StatusCode = [int]$resp.StatusCode
            Content    = $content
            Json       = $json
            Headers    = $resp.Headers
            Error      = $null
        }
    } catch {
        $ex = $_.Exception
        $statusCode = 0
        $errContent = ""
        if ($ex.Response) {
            $statusCode = [int]$ex.Response.StatusCode
            $reader = New-Object System.IO.StreamReader($ex.Response.GetResponseStream())
            $errContent = $reader.ReadToEnd()
        }
        $errJson = $null
        try { $errJson = $errContent | ConvertFrom-Json } catch {}
        return @{
            StatusCode = $statusCode
            Content    = $errContent
            Json       = $errJson
            Headers    = @{}
            Error      = $ex.Message
        }
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   HIRESMART ATS - COMPREHENSIVE E2E VERIFICATION SUITE   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# -------------------------------------------------------------
# PHASE 1: SERVER & STATIC WEB REACHABILITY
# -------------------------------------------------------------
Write-Host "`n>>> [PHASE 1] Static Web & Base Endpoints Check..." -ForegroundColor Yellow
$p1 = Invoke-Api -Method "GET" -Uri "/"
Record-Test "P1-01" "System" "Static index.html web app loads successfully (HTTP 200)" ($p1.StatusCode -eq 200 -and $p1.Content.Contains("HireSmart")) "Status: $($p1.StatusCode)"

$p1Stats = Invoke-Api -Method "GET" -Uri "/api/stats"
Record-Test "P1-02" "System" "Public dashboard stats endpoint returns 200 OK" ($p1Stats.StatusCode -eq 200 -and $null -ne $p1Stats.Json) "Status: $($p1Stats.StatusCode)"

# -------------------------------------------------------------
# PHASE 2: USER 1 (STUDENT / CANDIDATE) FULL WORKFLOW
# -------------------------------------------------------------
Write-Host "`n>>> [PHASE 2] Testing User 1 (Student / Candidate Workflow)..." -ForegroundColor Yellow

$u1Email = "alex.rivera" + (Get-Random -Minimum 1000 -Maximum 9999) + "@example.com"
$u1Pass = "SecurePass123!"
$u1Reg = @{
    fullName    = "Alex Rivera"
    email       = $u1Email
    password    = $u1Pass
    role        = "CANDIDATE"
    phone       = "+1 555-019-2834"
    headline    = "Senior Cloud Engineer | AWS & Kubernetes Specialist"
    location    = "San Francisco, CA"
}
$r1Reg = Invoke-Api -Method "POST" -Uri "/api/auth/register" -Body $u1Reg
$u1RegPass = ($r1Reg.StatusCode -eq 200 -and $r1Reg.Json.success -eq $true -and $null -ne $r1Reg.Json.token)
Record-Test "U1-01" "Candidate" "Register new candidate account ($u1Email)" $u1RegPass "Msg: $($r1Reg.Json.message)"

$u1Token = $r1Reg.Json.token
$u1User = $r1Reg.Json.user
$u1UserId = if ($u1User) { $u1User.id } else { 0 }
$u1Profile = $r1Reg.Json.profile
$u1ProfileId = if ($u1Profile) { $u1Profile.id } else { 0 }

# Login User 1
$u1Login = Invoke-Api -Method "POST" -Uri "/api/auth/login" -Body @{
    email    = $u1Email
    password = $u1Pass
}
$u1LoginPass = ($u1Login.StatusCode -eq 200 -and $u1Login.Json.success -eq $true)
Record-Test "U1-02" "Candidate" "Candidate login with valid credentials returns session token" $u1LoginPass "Status: $($u1Login.StatusCode)"
if ($u1LoginPass) {
    $u1Token = $u1Login.Json.token
    if ($u1Login.Json.user) { $u1UserId = $u1Login.Json.user.id }
    if ($u1Login.Json.profile) { $u1ProfileId = $u1Login.Json.profile.id }
}

$u1Headers = @{ "Authorization" = "Bearer $u1Token" }

# Validate Session
$u1Val = Invoke-Api -Method "GET" -Uri "/api/auth/validate" -Headers $u1Headers
Record-Test "U1-03" "Candidate" "Validate session with Bearer token (/api/auth/validate)" ($u1Val.StatusCode -eq 200 -and $u1Val.Json.valid -eq $true) "Valid: $($u1Val.Json.valid)"

# Get Profile by User ID
$u1ProfGet = Invoke-Api -Method "GET" -Uri "/api/candidates/profile/user/$u1UserId" -Headers $u1Headers
Record-Test "U1-04" "Candidate" "Fetch candidate profile by user ID" ($u1ProfGet.StatusCode -eq 200 -and $null -ne $u1ProfGet.Json) "Status: $($u1ProfGet.StatusCode)"

# Update Profile
$u1UpdatedHeadline = "Principal Cloud Architect | Distributed Systems & DevOps"
$u1ProfUpdatePayload = @{
    id                = $u1ProfileId
    userId            = $u1UserId
    fullName          = "Alex Rivera, M.S."
    headline          = $u1UpdatedHeadline
    location          = "Seattle, WA"
    qualification     = "M.S. in Computer Science"
    skills            = "Java, Spring Boot, AWS, Docker, Kubernetes, Terraform, MySQL"
    experienceYears   = 6
    experienceSummary = "6+ years designing resilient cloud architectures and high-throughput microservices."
    phone             = "+1 555-987-6543"
}
$u1ProfPut = Invoke-Api -Method "PUT" -Uri "/api/candidates/profile/$u1ProfileId" -Headers $u1Headers -Body $u1ProfUpdatePayload
Record-Test "U1-05" "Candidate" "Update candidate profile (/api/candidates/profile/{id})" ($u1ProfPut.StatusCode -eq 200) "Resp: $($u1ProfPut.Content)"

# Verify Updated Profile persisted
$u1ProfVerify = Invoke-Api -Method "GET" -Uri "/api/candidates/profile/$u1ProfileId" -Headers $u1Headers
$u1ProfVerified = ($u1ProfVerify.StatusCode -eq 200 -and $u1ProfVerify.Json.headline -eq $u1UpdatedHeadline)
Record-Test "U1-06" "Candidate" "Verify profile persistence after update" $u1ProfVerified "Headline: $($u1ProfVerify.Json.headline)"

# Browse Jobs
$jobsGet = Invoke-Api -Method "GET" -Uri "/api/jobs"
$firstJobId = if ($jobsGet.Json -and $jobsGet.Json.Count -gt 0) { $jobsGet.Json[0].id } else { 1 }
Record-Test "U1-07" "Candidate" "Browse active job listings (/api/jobs)" ($jobsGet.StatusCode -eq 200 -and $jobsGet.Json.Count -ge 1) "Jobs found: $($jobsGet.Json.Count)"

# Search Jobs
$jobsSearch = Invoke-Api -Method "GET" -Uri "/api/jobs/search?keyword=Java"
Record-Test "U1-08" "Candidate" "Search jobs with keyword 'Java' (/api/jobs/search)" ($jobsSearch.StatusCode -eq 200) "Results: $($jobsSearch.Json.Count)"

# Apply for Job
$u1AppPayload = @{
    candidateId = $u1ProfileId
    jobId       = $firstJobId
    resumeId    = 1
    coverNote   = "Excited to apply for this engineering leadership role!"
}
$u1Apply = Invoke-Api -Method "POST" -Uri "/api/applications" -Headers $u1Headers -Body $u1AppPayload
$u1AppSuccess = ($u1Apply.StatusCode -eq 200 -and $null -ne $u1Apply.Json.id)
$u1AppId = if ($u1AppSuccess) { $u1Apply.Json.id } else { 0 }
Record-Test "U1-09" "Candidate" "Submit job application (/api/applications)" $u1AppSuccess "App ID: $u1AppId"

# Fetch Candidate's Applications
$u1MyApp = Invoke-Api -Method "GET" -Uri "/api/applications/candidate/$u1ProfileId" -Headers $u1Headers
Record-Test "U1-10" "Candidate" "Candidate fetches their own applications (/api/applications/candidate/{id})" ($u1MyApp.StatusCode -eq 200 -and $u1MyApp.Json.Count -ge 1) "Apps found: $($u1MyApp.Json.Count)"

# Mock Interview Questions
$mockQ = Invoke-Api -Method "GET" -Uri "/api/mock-interview/questions"
Record-Test "U1-11" "Candidate" "Candidate retrieves mock interview questions" ($mockQ.StatusCode -eq 200 -and $mockQ.Json.Count -ge 1) "Questions: $($mockQ.Json.Count)"

# Submit Mock Answer
$firstQId = if ($mockQ.Json -and $mockQ.Json.Count -gt 0) { $mockQ.Json[0].id } else { 1 }
$mockAnsPayload = @{
    candidateId     = $u1ProfileId
    questionId      = $firstQId
    candidateAnswer = "Spring Boot microservices achieve high scalability through stateless architecture, REST/gRPC communication, Redis distributed caching, and containerization with Kubernetes."
}
$mockSub = Invoke-Api -Method "POST" -Uri "/api/mock-interview/submit" -Headers $u1Headers -Body $mockAnsPayload
Record-Test "U1-12" "Candidate" "Submit and evaluate AI mock interview answer" ($mockSub.StatusCode -eq 200 -and $mockSub.Json.score -gt 0) "Score: $($mockSub.Json.score)"

# Notifications
$notifs = Invoke-Api -Method "GET" -Uri "/api/notifications?email=$u1Email" -Headers $u1Headers
Record-Test "U1-13" "Candidate" "Candidate fetches their notification inbox" ($notifs.StatusCode -eq 200) "Status: $($notifs.StatusCode)"

# User 1 Logout
$u1Logout = Invoke-Api -Method "POST" -Uri "/api/auth/logout" -Headers $u1Headers
Record-Test "U1-14" "Candidate" "User 1 logs out successfully and terminates session" ($u1Logout.StatusCode -eq 200) "Msg: $($u1Logout.Json.message)"

# -------------------------------------------------------------
# PHASE 3: USER 2 (RECRUITER) FULL WORKFLOW
# -------------------------------------------------------------
Write-Host "`n>>> [PHASE 3] Testing User 2 (Recruiter Workflow)..." -ForegroundColor Yellow

$u2Email = "recruiter.techcorp" + (Get-Random -Minimum 1000 -Maximum 9999) + "@example.com"
$u2Pass = "TalentLeader2026!"
$u2Reg = @{
    fullName    = "Elena Rostova"
    companyName = "Apex Global Technologies"
    email       = $u2Email
    password    = $u2Pass
    role        = "RECRUITER"
    phone       = "+1 415-555-7788"
}
$r2Reg = Invoke-Api -Method "POST" -Uri "/api/auth/register" -Body $u2Reg
Record-Test "U2-01" "Recruiter" "Register new recruiter account ($u2Email)" ($r2Reg.StatusCode -eq 200 -and $r2Reg.Json.success -eq $true) "Msg: $($r2Reg.Json.message)"

$u2Token = $r2Reg.Json.token
$u2Headers = @{ "Authorization" = "Bearer $u2Token" }

# Create New Job Posting
$newJobPayload = @{
    title              = "Lead Cloud Architect (AWS/K8s)"
    companyName        = "Apex Global Technologies"
    department         = "Cloud Platform Engineering"
    location           = "San Francisco, CA / Remote"
    jobType            = "FULL_TIME"
    requiredSkills     = "Java, Spring Boot, AWS, Docker, Kubernetes, MySQL, Terraform"
    experienceRequired = 5
    description        = "Lead the architecture and implementation of scalable, fault-tolerant enterprise cloud platforms."
    status             = "ACTIVE"
}
$u2JobPost = Invoke-Api -Method "POST" -Uri "/api/jobs" -Headers $u2Headers -Body $newJobPayload
$u2JobSuccess = ($u2JobPost.StatusCode -eq 200 -and $null -ne $u2JobPost.Json.id)
$createdJobId = if ($u2JobSuccess) { $u2JobPost.Json.id } else { 0 }
Record-Test "U2-02" "Recruiter" "Recruiter posts new job opening (/api/jobs)" $u2JobSuccess "Job ID: $createdJobId"

# Update Job Posting
$jobUpdatePayload = @{
    id                 = $createdJobId
    title              = "Principal Cloud Architect (AWS/K8s)"
    companyName        = "Apex Global Technologies"
    department         = "Cloud Platform Engineering"
    location           = "San Francisco, CA / Hybrid"
    jobType            = "FULL_TIME"
    requiredSkills     = "Java, Spring Boot, AWS, Docker, Kubernetes, MySQL, Terraform"
    experienceRequired = 6
    description        = "Lead cloud architecture across multiple global engineering pods."
    status             = "ACTIVE"
}
$u2JobPut = Invoke-Api -Method "PUT" -Uri "/api/jobs/$createdJobId" -Headers $u2Headers -Body $jobUpdatePayload
Record-Test "U2-03" "Recruiter" "Recruiter updates existing job requisition (/api/jobs/{id})" ($u2JobPut.StatusCode -eq 200) "Resp: $($u2JobPut.Content)"

# Recruiter views all applications pipeline
$u2AllApps = Invoke-Api -Method "GET" -Uri "/api/applications" -Headers $u2Headers
Record-Test "U2-04" "Recruiter" "Recruiter views all applications pipeline (/api/applications)" ($u2AllApps.StatusCode -eq 200 -and $u2AllApps.Json.Count -ge 1) "Total apps: $($u2AllApps.Json.Count)"

# Recruiter updates candidate application status
$targetAppId = if ($u1AppId -gt 0) { $u1AppId } else { 1 }
$statusPayload = @{
    status            = "SHORTLISTED"
    newStatus         = "SHORTLISTED"
    changeReason      = "Candidate possesses strong cloud architecture experience."
    recruiterNotes    = "Scheduled for Technical Round 1 screening."
    changedByUserId   = 2
}
$u2StatusPut = Invoke-Api -Method "PUT" -Uri "/api/applications/$targetAppId/status" -Headers $u2Headers -Body $statusPayload
Record-Test "U2-05" "Recruiter" "Recruiter updates candidate application status to SHORTLISTED" ($u2StatusPut.StatusCode -eq 200) "Resp: $($u2StatusPut.Content)"

# Recruiter schedules interview
$interviewPayload = @{
    candidateId        = $u1ProfileId
    jobId              = $firstJobId
    applicationId      = $targetAppId
    interviewType      = "TECHNICAL"
    interviewDate      = (Get-Date).AddDays(2).ToString("yyyy-MM-ddTHH:mm:ss")
    interviewerName    = "Elena Rostova (Lead Recruiter)"
    meetingLinkOrVenue = "https://meet.google.com/hsm-apex-tech"
    notes              = "Focus on cloud microservices tradeoffs and distributed database design."
}
$u2InterviewSched = Invoke-Api -Method "POST" -Uri "/api/interviews/schedule" -Headers $u2Headers -Body $interviewPayload
$interviewId = if ($u2InterviewSched.StatusCode -eq 200 -and $u2InterviewSched.Json) { $u2InterviewSched.Json.id } else { 1 }
Record-Test "U2-06" "Recruiter" "Recruiter schedules technical interview (/api/interviews/schedule)" ($u2InterviewSched.StatusCode -eq 200) "Interview ID: $interviewId"

# Recruiter views all interviews
$u2Interviews = Invoke-Api -Method "GET" -Uri "/api/interviews" -Headers $u2Headers
Record-Test "U2-07" "Recruiter" "Recruiter views interviews directory (/api/interviews)" ($u2Interviews.StatusCode -eq 200 -and $u2Interviews.Json.Count -ge 1) "Interviews: $($u2Interviews.Json.Count)"

# Recruiter submits interview feedback
$feedbackPayload = @{
    technicalScore      = 92
    communicationScore  = 90
    problemSolvingScore = 95
    evaluatorNotes      = "Exceptional command of distributed systems, concurrency, and cloud architectures."
    recommendation      = "HIRE"
}
$u2Feedback = Invoke-Api -Method "POST" -Uri "/api/interviews/$interviewId/feedback" -Headers $u2Headers -Body $feedbackPayload
Record-Test "U2-08" "Recruiter" "Recruiter submits comprehensive candidate evaluation feedback" ($u2Feedback.StatusCode -eq 200) "Resp: $($u2Feedback.Content)"

# AI Resume Screening Studio
$screenPayload = @{
    candidateId = $u1ProfileId
    jobId       = $firstJobId
    resumeText  = "Senior Cloud Architect with 6 years experience in Java, Spring Boot, AWS, Docker, Kubernetes, Terraform, and MySQL."
}
$u2Screen = Invoke-Api -Method "POST" -Uri "/api/screening/screen" -Headers $u2Headers -Body $screenPayload
Record-Test "U2-09" "Recruiter" "Recruiter executes AI resume screening match analysis" ($u2Screen.StatusCode -eq 200 -and $u2Screen.Json.success -eq $true) "Match: $($u2Screen.Json.score)%"

# Excel Reports Download
$u2ReportCandidates = Invoke-Api -Method "GET" -Uri "/api/reports/download/candidates" -Headers $u2Headers
Record-Test "U2-10" "Recruiter" "Recruiter downloads Candidates Excel report (XLSX binary)" ($u2ReportCandidates.StatusCode -eq 200 -and $u2ReportCandidates.Content.Length -gt 100) "Bytes: $($u2ReportCandidates.Content.Length)"

$u2ReportJobs = Invoke-Api -Method "GET" -Uri "/api/reports/download/jobs" -Headers $u2Headers
Record-Test "U2-11" "Recruiter" "Recruiter downloads Jobs Requisition Excel report" ($u2ReportJobs.StatusCode -eq 200) "Bytes: $($u2ReportJobs.Content.Length)"

# Verify Recruiter CANNOT modify candidate's personal profile (403 Forbidden)
$u2MaliciousProfilePut = Invoke-Api -Method "PUT" -Uri "/api/candidates/profile/$u1ProfileId" -Headers $u2Headers -Body @{
    id = $u1ProfileId
    headline = "Hacked by Recruiter"
}
Record-Test "U2-12" "Security" "Recruiter blocked from modifying candidate personal profile (403 Forbidden)" ($u2MaliciousProfilePut.StatusCode -eq 403) "Status: $($u2MaliciousProfilePut.StatusCode)"

# -------------------------------------------------------------
# PHASE 4: USER 3 (NEGATIVE, VALIDATION & SECURITY TESTS)
# -------------------------------------------------------------
Write-Host "`n>>> [PHASE 4] Testing User 3 (Negative Scenarios, RBAC & Security)..." -ForegroundColor Yellow

# Duplicate email registration
$u3DupReg = Invoke-Api -Method "POST" -Uri "/api/auth/register" -Body @{
    fullName = "Duplicate User"
    email    = $u1Email
    password = "password123"
    role     = "CANDIDATE"
}
Record-Test "U3-01" "Negative" "Reject duplicate email registration with clear error message" ($u3DupReg.StatusCode -eq 200 -and $u3DupReg.Json.success -eq $false) "Msg: $($u3DupReg.Json.message)"

# Invalid email format
$u3BadEmailReg = Invoke-Api -Method "POST" -Uri "/api/auth/register" -Body @{
    fullName = "Bad Email User"
    email    = "not-an-email-address"
    password = "password123"
    role     = "CANDIDATE"
}
Record-Test "U3-02" "Negative" "Reject malformed email address on registration" ($u3BadEmailReg.StatusCode -eq 200 -and $u3BadEmailReg.Json.success -eq $false) "Msg: $($u3BadEmailReg.Json.message)"

# Weak/short password (< 6 chars)
$u3ShortPass = Invoke-Api -Method "POST" -Uri "/api/auth/register" -Body @{
    fullName = "Short Pass User"
    email    = "shortpass" + (Get-Random) + "@example.com"
    password = "123"
    role     = "CANDIDATE"
}
Record-Test "U3-03" "Negative" "Reject weak passwords under 6 characters" ($u3ShortPass.StatusCode -eq 200 -and $u3ShortPass.Json.success -eq $false) "Msg: $($u3ShortPass.Json.message)"

# Recruiter without company name
$u3NoCompany = Invoke-Api -Method "POST" -Uri "/api/auth/register" -Body @{
    fullName    = "No Company Recruiter"
    email       = "nocompany" + (Get-Random) + "@example.com"
    password    = "password123"
    role        = "RECRUITER"
    companyName = ""
}
Record-Test "U3-04" "Negative" "Reject recruiter registration missing mandatory company name" ($u3NoCompany.StatusCode -eq 200 -and $u3NoCompany.Json.success -eq $false) "Msg: $($u3NoCompany.Json.message)"

# Block public self-registration with ADMIN role
$u3AdminReg = Invoke-Api -Method "POST" -Uri "/api/auth/register" -Body @{
    fullName = "Fake Admin"
    email    = "fakeadmin" + (Get-Random) + "@example.com"
    password = "password123"
    role     = "ADMIN"
}
Record-Test "U3-05" "Security" "Block public self-registration for ADMIN role" ($u3AdminReg.StatusCode -eq 200 -and $u3AdminReg.Json.success -eq $false) "Msg: $($u3AdminReg.Json.message)"

# Login with wrong password
$u3WrongPass = Invoke-Api -Method "POST" -Uri "/api/auth/login" -Body @{
    email    = $u1Email
    password = "CompletelyWrongPassword!"
}
Record-Test "U3-06" "Security" "Reject login attempt with invalid password" ($u3WrongPass.StatusCode -eq 200 -and $u3WrongPass.Json.success -eq $false) "Msg: $($u3WrongPass.Json.message)"

# Candidate trying Recruiter Action (POST /api/jobs)
$candLoginAgain = Invoke-Api -Method "POST" -Uri "/api/auth/login" -Body @{ email = $u1Email; password = $u1Pass }
$candToken = $candLoginAgain.Json.token
$candHeaders = @{ "Authorization" = "Bearer $candToken" }

$candIllegalJobPost = Invoke-Api -Method "POST" -Uri "/api/jobs" -Headers $candHeaders -Body @{ title = "Illegal Job"; description = "Hack" }
Record-Test "U3-07" "RBAC" "Candidate blocked from posting jobs (403 Forbidden)" ($candIllegalJobPost.StatusCode -eq 403) "Status: $($candIllegalJobPost.StatusCode)"

# Candidate trying to view all applications pipeline
$candIllegalApps = Invoke-Api -Method "GET" -Uri "/api/applications" -Headers $candHeaders
Record-Test "U3-08" "RBAC" "Candidate blocked from viewing all pipeline applications (403 Forbidden)" ($candIllegalApps.StatusCode -eq 403) "Status: $($candIllegalApps.StatusCode)"

# Candidate trying to change application status
$candIllegalStatus = Invoke-Api -Method "PUT" -Uri "/api/applications/1/status" -Headers $candHeaders -Body @{ status = "SELECTED" }
Record-Test "U3-09" "RBAC" "Candidate blocked from modifying hiring status (403 Forbidden)" ($candIllegalStatus.StatusCode -eq 403) "Status: $($candIllegalStatus.StatusCode)"

# Candidate trying to access Admin API
$candIllegalAdmin = Invoke-Api -Method "GET" -Uri "/api/admin/users" -Headers $candHeaders
Record-Test "U3-10" "RBAC" "Candidate blocked from accessing Admin endpoints (403 Forbidden)" ($candIllegalAdmin.StatusCode -eq 403) "Status: $($candIllegalAdmin.StatusCode)"

# Recruiter trying to access Admin API
$recIllegalAdmin = Invoke-Api -Method "GET" -Uri "/api/admin/users" -Headers $u2Headers
Record-Test "U3-11" "RBAC" "Recruiter blocked from accessing Admin endpoints (403 Forbidden)" ($recIllegalAdmin.StatusCode -eq 403) "Status: $($recIllegalAdmin.StatusCode)"

# Unauthenticated access to protected endpoint
$unauthReq = Invoke-Api -Method "GET" -Uri "/api/admin/users"
Record-Test "U3-12" "Security" "Reject unauthenticated requests to protected APIs (401 Unauthorized)" ($unauthReq.StatusCode -eq 401) "Status: $($unauthReq.StatusCode)"

# Tampered / Invalid Bearer token
$fakeTokenReq = Invoke-Api -Method "GET" -Uri "/api/applications" -Headers @{ "Authorization" = "Bearer fake-invalid-token-uuid-12345" }
Record-Test "U3-13" "Security" "Reject forged or expired bearer tokens (401 Unauthorized)" ($fakeTokenReq.StatusCode -eq 401) "Status: $($fakeTokenReq.StatusCode)"

# -------------------------------------------------------------
# PHASE 5: USER 4 (SYSTEM ADMINISTRATOR) FULL WORKFLOW
# -------------------------------------------------------------
Write-Host "`n>>> [PHASE 5] Testing User 4 (System Administrator Workflow)..." -ForegroundColor Yellow

$adminLogin = Invoke-Api -Method "POST" -Uri "/api/auth/login" -Body @{
    email        = "admin@hiresmart.com"
    password     = "admin123"
    expectedRole = "ADMIN"
}
$adminLoginPass = ($adminLogin.StatusCode -eq 200 -and $adminLogin.Json.success -eq $true)
Record-Test "U4-01" "Admin" "System Administrator login with admin123" $adminLoginPass "Msg: $($adminLogin.Json.message)"

$adminToken = $adminLogin.Json.token
$adminHeaders = @{ "Authorization" = "Bearer $adminToken" }

# Admin fetches all users
$adminUsers = Invoke-Api -Method "GET" -Uri "/api/admin/users" -Headers $adminHeaders
Record-Test "U4-02" "Admin" "Admin lists all registered platform users (/api/admin/users)" ($adminUsers.StatusCode -eq 200 -and $adminUsers.Json.Count -ge 2) "Users found: $($adminUsers.Json.Count)"

# Verify password hashes are NEVER leaked in user list
$hashLeaked = $false
if ($adminUsers.Json) {
    foreach ($u in $adminUsers.Json) {
        if ($null -ne $u.passwordHash -and $u.passwordHash -ne "") {
            $hashLeaked = $true
        }
    }
}
Record-Test "U4-03" "Security" "Verify user password hashes are sanitized (null) in API responses" (-not $hashLeaked) "Hash leaked: $hashLeaked"

# Admin filters users by role
$adminCandUsers = Invoke-Api -Method "GET" -Uri "/api/admin/users?role=CANDIDATE" -Headers $adminHeaders
Record-Test "U4-04" "Admin" "Admin filters users by role=CANDIDATE" ($adminCandUsers.StatusCode -eq 200 -and $adminCandUsers.Json.Count -ge 1) "Candidates: $($adminCandUsers.Json.Count)"

# Admin toggles user status (deactivate & reactivate)
$toggleUserId = if ($u1UserId -gt 0) { $u1UserId } else { 1 }
$deactivate = Invoke-Api -Method "PUT" -Uri "/api/admin/users/$toggleUserId/status?active=false" -Headers $adminHeaders
Record-Test "U4-05" "Admin" "Admin deactivates user account (/api/admin/users/{id}/status)" ($deactivate.StatusCode -eq 200 -and $deactivate.Json.active -eq $false) "Resp: $($deactivate.Content)"

# Deactivated user cannot log in
$deactLogin = Invoke-Api -Method "POST" -Uri "/api/auth/login" -Body @{ email = $u1Email; password = $u1Pass }
Record-Test "U4-06" "Security" "Deactivated user account login blocked" ($deactLogin.StatusCode -eq 200 -and $deactLogin.Json.success -eq $false -and $deactLogin.Json.message.Contains("deactivated")) "Msg: $($deactLogin.Json.message)"

# Re-activate user
$reactivate = Invoke-Api -Method "PUT" -Uri "/api/admin/users/$toggleUserId/status?active=true" -Headers $adminHeaders
Record-Test "U4-07" "Admin" "Admin reactivates user account" ($reactivate.StatusCode -eq 200 -and $reactivate.Json.active -eq $true) "Resp: $($reactivate.Content)"

# Admin views audit logs
$adminLogs = Invoke-Api -Method "GET" -Uri "/api/admin/audit-logs?limit=20" -Headers $adminHeaders
Record-Test "U4-08" "Admin" "Admin inspects security audit logs (/api/admin/audit-logs)" ($adminLogs.StatusCode -eq 200 -and $adminLogs.Json.Count -ge 1) "Logs count: $($adminLogs.Json.Count)"

# Admin logs out
$adminLogout = Invoke-Api -Method "POST" -Uri "/api/auth/logout" -Headers $adminHeaders
Record-Test "U4-09" "Admin" "Admin logs out and invalidates session token" ($adminLogout.StatusCode -eq 200) "Msg: $($adminLogout.Json.message)"

# Verify Admin token is invalidated
$adminSubsequentReq = Invoke-Api -Method "GET" -Uri "/api/admin/users" -Headers $adminHeaders
Record-Test "U4-10" "Security" "Terminated Admin token rejected on subsequent API call (401)" ($adminSubsequentReq.StatusCode -eq 401) "Status: $($adminSubsequentReq.StatusCode)"

# -------------------------------------------------------------
# PHASE 6: SUMMARY REPORT
# -------------------------------------------------------------
Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "                TEST EXECUTION SUMMARY                    " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
$passCount = ($results | Where-Object { $_.Passed }).Count
$failCount = ($results | Where-Object { -not $_.Passed }).Count
$totalCount = $results.Count

Write-Host "Total Tests Run : $totalCount"
Write-Host "Passed          : $passCount" -ForegroundColor Green
Write-Host "Failed          : $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host "Pass Rate       : $([Math]::Round(($passCount / $totalCount) * 100, 1))%" -ForegroundColor Yellow

if ($failCount -gt 0) {
    Write-Host "`nFailed Tests Breakdown:" -ForegroundColor Red
    $results | Where-Object { -not $_.Passed } | Format-Table TestId, Category, Description, Details -AutoSize
}
