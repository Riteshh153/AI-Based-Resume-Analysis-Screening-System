$baseUrl = "http://localhost:8085"

Write-Host "--- STEP 1: CANDIDATE LOGIN ---"
$loginBody = @{
    email = "candidate@hiresmart.com"
    password = "password123"
    expectedRole = "CANDIDATE"
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
Write-Host "Candidate Login: $($loginRes.success), User: $($loginRes.user.email), Token: $($loginRes.token.Substring(0,8))..."

$headers = @{
    "Authorization" = "Bearer $($loginRes.token)"
    "Content-Type" = "application/json"
}

Write-Host "`n--- STEP 2: PROFILE UPDATE VIA PUT ---"
$profBody = @{
    id = 3
    userId = 3
    fullName = "Aditya Verma"
    headline = "Full Stack Java & Microservices Specialist"
    location = "Bangalore, India"
    skills = "Java, Spring Boot, MySQL, REST APIs, Microservices, Docker, Kubernetes, AWS"
    qualification = "B.Tech Computer Science (8.8 CGPA)"
    experienceYears = 3
    experienceSummary = "Engineered scalable cloud microservices."
} | ConvertTo-Json

$profRes = Invoke-RestMethod -Uri "$baseUrl/api/candidates/profile/3" -Method Put -Headers $headers -Body $profBody
Write-Host "Profile Update Success: $($profRes.id -ne $null), Updated Headline: $($profRes.headline)"

Write-Host "`n--- STEP 3: CANDIDATE JOB FIT SCREENING VIA POST /api/screening/screen ---"
$jobs = Invoke-RestMethod -Uri "$baseUrl/api/jobs"
$targetJob = $jobs[0]
Write-Host "Comparing against Job: $($targetJob.title) (ID: $($targetJob.id))"

$screenBody = @{
    jobId = $targetJob.id
    candidateId = 3
    resumeText = "Java Spring Boot MySQL REST APIs Microservices Docker Kubernetes AWS"
} | ConvertTo-Json

$screenRes = Invoke-RestMethod -Uri "$baseUrl/api/screening/screen" -Method Post -Headers $headers -Body $screenBody
Write-Host "Screening Success: $($screenRes.success), Match Score: $($screenRes.score)%"
Write-Host "Matched Skills: $($screenRes.matchedSkillsList -join ', ')"
Write-Host "Missing Skills: $($screenRes.missingSkillsList -join ', ')"
