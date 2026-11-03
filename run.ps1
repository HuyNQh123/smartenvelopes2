Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   KHOI DONG HE THONG SMARTENVELOPE (SPRING BOOT)      " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# 1. Nap cac bien moi truong tu file .env neu co
if (Test-Path .env) {
    Write-Host "Dang doc cau hinh tu file .env..." -ForegroundColor Yellow
    Get-Content .env | Where-Object { $_ -match '^\s*([^#][^=]+)=(.*)$' } | ForEach-Object {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

# 2. Xac dinh duong dan Maven tren may
$nbMaven = "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"

if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "Dang su dung Maven tu PATH he thong..." -ForegroundColor Green
    mvn spring-boot:run
} elseif (Test-Path $nbMaven) {
    Write-Host "Dang su dung Maven co san tai: $nbMaven" -ForegroundColor Green
    & $nbMaven spring-boot:run
} else {
    Write-Host "[LOI] Khong tim thay Maven tren may tinh." -ForegroundColor Red
    Write-Host "Vui long kiem tra bien moi truong PATH hoac chay truc tiep tu IntelliJ IDEA." -ForegroundColor Yellow
}
