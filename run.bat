@echo off
echo ========================================================
echo   KHOI DONG HE THONG SMARTENVELOPE (SPRING BOOT)
echo ========================================================

REM 1. Nap cac bien moi truong tu file .env neu co
if exist .env (
    echo Dang doc cau hinh tu file .env...
    for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
        set "%%A=%%B"
    )
)

REM 2. Xac dinh duong dan Maven tren may
set "NB_MAVEN=C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo Dang su dung Maven tu PATH he thong...
    mvn spring-boot:run
) else if exist "%NB_MAVEN%" (
    echo Dang su dung Maven co san tai: %NB_MAVEN%
    "%NB_MAVEN%" spring-boot:run
) else (
    echo [LOI] Khong tim thay Maven tren may tinh.
    echo Vui long kiem tra lai duong dan Maven hoac chay truc tiep tu IntelliJ IDEA / NetBeans.
    pause
)
