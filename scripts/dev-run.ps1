# English: dev helper to start Spring Boot with dev profile
function New-RandomSecret([int]$bytes = 48) {
    $buffer = New-Object byte[] ($bytes)
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($buffer)
    return [Convert]::ToBase64String($buffer)
}

$env:SPRING_PROFILES_ACTIVE = "dev"

if (-not $env:APP_SECURITY_JWT_SECRET -or $env:APP_SECURITY_JWT_SECRET -eq "change_this_in_env") {
    Write-Host "Generating ephemeral JWT secret for dev session."
    $env:APP_SECURITY_JWT_SECRET = New-RandomSecret 48
}

if (-not $env:APP_SECURITY_USER_PASSWORD) {
    $env:APP_SECURITY_USER_PASSWORD = "DevUserPass123!"
}

if (-not $env:APP_SECURITY_ADMIN_PASSWORD) {
    $env:APP_SECURITY_ADMIN_PASSWORD = "DevAdminPass123!"
}

if (-not $env:APP_SECURITY_REQUIRE_HTTPS) {
    $env:APP_SECURITY_REQUIRE_HTTPS = "false"
}

mvn spring-boot:run
