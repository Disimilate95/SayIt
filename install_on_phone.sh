#!/usr/bin/env bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APK_PATH="$DIR/SayIt.apk"

echo "========================================================"
echo "       Установщик приложения SayIt на Android"
echo "========================================================"
echo ""

if ! command -v adb &> /dev/null; then
    echo "[!] Утилита 'adb' не найдена в системе."
    echo ""
    echo "Самый простой способ установить приложение:"
    echo "1. Отправьте файл 'SayIt.apk' себе в Telegram, WhatsApp или залейте на Google Диск."
    echo "2. На телефоне скачайте SayIt.apk и нажмите на него."
    echo "3. В появившемся окне подтвердите установку."
    echo ""
    exit 1
fi

echo "Список подключенных устройств:"
adb devices
echo ""

if [ ! -f "$APK_PATH" ]; then
    echo "[!] Файл $APK_PATH не найден. Попробуем путь из сборки Gradle..."
    APK_PATH="$DIR/app/build/outputs/apk/debug/app-debug.apk"
fi

if [ ! -f "$APK_PATH" ]; then
    echo "[!] APK файл не найден. Запустите: gradle assembleDebug"
    exit 1
fi

echo "Установка SayIt ($APK_PATH) на устройство..."
adb install -r "$APK_PATH"

echo ""
echo "========================================================"
echo "[УСПЕХ] SayIt успешно установлено на ваше устройство!"
echo "========================================================"
echo "Запуск приложения..."
adb shell monkey -p com.aistudio.sayit.kxmpzq -c android.intent.category.LAUNCHER 1 || true
