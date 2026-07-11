# Loads variables from .env into this PowerShell session, then starts the
# Spring Boot backend. The backend reads ANTHROPIC_API_KEY from the environment
# (application.properties: crm.ai.api-key=${ANTHROPIC_API_KEY:}), so the AI
# chatbot and course-recommendation quiz only work when the key is present.
#
# Usage:   .\run-backend.ps1

if (Test-Path .env) {
    Get-Content .env | ForEach-Object {
        $line = $_.Trim()
        # Skip blanks and comments; require a KEY=VALUE line.
        if ($line -and -not $line.StartsWith('#') -and $line.Contains('=')) {
            $idx   = $line.IndexOf('=')                       # split on FIRST '=' only
            $name  = $line.Substring(0, $idx).Trim()
            $value = $line.Substring($idx + 1).Trim().Trim('"')
            if ($name) {
                Set-Item -Path "env:$name" -Value $value
            }
        }
    }
    if ($env:ANTHROPIC_API_KEY) {
        Write-Host "Loaded .env (ANTHROPIC_API_KEY is set) - AI features enabled." -ForegroundColor Green
    } else {
        Write-Host "Loaded .env, but ANTHROPIC_API_KEY is empty - AI features disabled." -ForegroundColor Yellow
    }
} else {
    Write-Host "No .env file found - AI features will be disabled." -ForegroundColor Yellow
}

.\mvnw.cmd spring-boot:run
