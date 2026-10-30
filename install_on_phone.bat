@echo off
chcp 65001 >nul
echo ========================================================
echo        Установщик приложения SayIt на Android
echo ========================================================
echo.

where adb >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [!] Утилита 'adb' (Android Debug Bridge) не найдена в PATH.
    echo.
    echo Простой способ установить без компьютера:
    echo 1. Скопируйте файл 'SayIt.apk' на телефон (через USB, Telegram, Google Диск и т.д.).
    echo 2. На телефоне откройте 'Файлы' / 'Загрузки' и нажмите на SayIt.apk.
    echo 3. Разрешите установку из этого источника и нажмите 'Установить'.
    echo.
    pause
    exit /b 1
)

echo Поиск подключенного устройства...
adb devices
echo.
echo Проверьте, включена ли "Отладка по USB" в настройках телефона:
echo (Настройки -> Для разработчиков -> Отладка по USB).
echo.

echo Установка SayIt.apk на телефон...
adb install -r "%~dp0SayIt.apk"

if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================================
    echo [УСПЕХ] Приложение SayIt успешно установлено на телефон!
    echo ========================================================
    echo Запуск приложения...
    adb shell monkey -p com.aistudio.sayit.kxmpzq -c android.intent.category.LAUNCHER 1 >nul 2>nul
) else (
    echo.
    echo [ОШИБКА] Не удалось установить приложение через ADB.
    echo Проверьте подключение кабеля и подтвердите запрос "Разрешить отладку" на экране телефона.
    echo.
    echo Или просто перекиньте файл SayIt.apk напрямую на телефон и откройте его.
)

pause
