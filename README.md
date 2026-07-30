# CyberGym

CyberGym — Android-приложение для персональных тренировок в Counter-Strike 2. Оно формирует тренировочный план, учитывает уровень и цели игрока, получает статистику FACEIT и сохраняет результаты упражнений с карты CS2 Workshop.

Проект находится на стадии MVP и активно развивается.

## Возможности

- регистрация и авторизация через Supabase;
- подробный CS2-онбординг с сохранением ответов;
- подключение FACEIT и загрузка ELO, уровня, K/D, win rate и другой статистики;
- персональный ежедневный план и каталог отдельных упражнений;
- запуск и завершение тренировочных сессий;
- встроенный QR-сканер CyberGym на CameraX;
- просмотр и редактирование результатов Workshop перед сохранением;
- XP, уровни, серии тренировок и экран прогресса;
- профиль игрока и сохранение снимка FACEIT-статистики.

## Сценарий Workshop

```text
CyberGym → тренировочный план → карта CS2 Workshop
         → QR-код с результатами → встроенный сканер
         → проверка показателей → Supabase → XP и прогресс
```

После прохождения карты пользователь нажимает «Завершить тренировку». Приложение открывает собственный экран камеры, распознаёт QR-код, показывает метрики и позволяет исправить их. Сессия закрывается только после подтверждения.

Протокол QR описан в [CyberGym_QR_Result_Protocol.md](workshop/CyberGym_QR_Result_Protocol.md). Готовые тестовые коды и генератор находятся в [workshop/qr_samples](workshop/qr_samples/README.md).

## Технологии

- Kotlin 2.2 и Java 11;
- Jetpack Compose и Material 3;
- Navigation Compose;
- Koin;
- Supabase Kotlin SDK, Auth и PostgREST;
- CameraX и bundled ML Kit Barcode Scanning;
- Kotlin Serialization, Ktor и Coil;
- JUnit, MockK, Turbine, Detekt и ktlint.

Минимальная версия Android — API 28. `compileSdk` и `targetSdk` — 36.

## Структура проекта

```text
app/                    точка входа, навигация, manifest и DI
core/
  common/               общие модели, ошибки и Result
  designsystem/         тема и переиспользуемые Compose-компоненты
  navigation/           маршруты
  network/              Supabase-клиент и сетевые зависимости
  analytics/            события аналитики
feature/
  auth/                 вход и регистрация
  onboarding/           настройка игрового профиля
  home/                 экран «Сегодня»
  training/             трек, сессия и QR-результаты
  progress/             статистика прогресса
  profile/              профиль и FACEIT
supabase/
  migrations/           схема, RLS и RPC
  functions/            серверные FACEIT-прокси
workshop/               документация карты и QR-протокол
```

## Локальный запуск

### Требования

- Android Studio с JDK 17 для запуска Gradle;
- Android SDK 36;
- устройство или эмулятор с API 28+;
- доступный проект Supabase;
- физическое устройство для полноценной проверки камеры и QR-сканера.

### Настройка Supabase

Скопируйте шаблон:

```powershell
Copy-Item local.properties.template local.properties
```

Заполните `local.properties`:

```properties
supabase_url=https://supabase.example.com
supabase_anon_key=your-public-anon-key
```

URL должен содержать `https://`. В Android-приложение разрешено помещать только публичный `anon` key. Пароли базы, `service_role` и FACEIT API key нельзя добавлять в `local.properties` или Git.

### Сборка

Из корня проекта:

```powershell
.\gradlew.bat :app:assembleDebug
```

APK будет создан в:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Запустить основные проверки:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat detekt ktlintCheck
```

Инструментальные тесты требуют подключённое устройство:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## База данных

SQL-файлы находятся в [supabase/migrations](supabase/migrations) и применяются по имени файла, от старого к новому. Для существующей базы не нужно повторно выполнять уже применённые миграции.

Последняя миграция Workshop:

```text
20260728120000_workshop_qr_results.sql
```

Она добавляет:

- RPC версии `v2` для запуска и завершения тренировок;
- хранение результатов одиночных упражнений;
- проверку соответствия QR текущей сессии;
- защиту от повторного использования одного `run_id`.

Для self-hosted Supabase используйте правильный контейнер CyberGym и `ON_ERROR_STOP=1`. Не применяйте миграции к контейнеру другого проекта. Подробная инструкция: [SUPABASE_VPS_DEPLOYMENT.md](SUPABASE_VPS_DEPLOYMENT.md).

## FACEIT

FACEIT API key хранится только на сервере в переменной окружения:

```text
FACEIT_API_KEY
```

Android-приложение обращается к авторизованным Supabase Edge Functions:

```text
/functions/v1/faceit-player?nickname=...
/functions/v1/faceit-stats?player_id=...
```

Исходники функций:

- [faceit-player](supabase/functions/faceit-player/index.ts);
- [faceit-stats](supabase/functions/faceit-stats/index.ts).

После изменения функций убедитесь, что они доступны в роутере self-hosted Edge Runtime, а переменная `FACEIT_API_KEY` видна внутри контейнера Functions.

## QR-сканер

Приложение использует собственный Compose-интерфейс камеры. CameraX управляет камерой, а bundled ML Kit распознаёт только QR-коды локально. Модель входит в APK и не требует загрузки при первом сканировании.

Android запросит разрешение:

```xml
android.permission.CAMERA
```

Если пользователь запретил его, разрешение нужно вернуть в системных настройках приложения.

## Документация

- [Техническое и продуктовое описание MVP](nextrank-agent-pack/README.md)
- [Экраны приложения](screen/cs2_training_app_mvp_screens.md)
- [Онбординг](Onboarding/CS2_Onboarding_MVP.md)
- [Спецификация Workshop-карты](workshop/CyberGym_CS2_Workshop_Map_MVP.md)
- [Mapper guide](workshop/CyberGym_CS2_Mapper_Guide.md)
- [Зона counter-strafe](workshop/CyberGym_CS2_Strafe_Zone_Guide.md)
- [QR Result Protocol](workshop/CyberGym_QR_Result_Protocol.md)

## Безопасность

- не коммитьте `local.properties`, `.env`, ключи FACEIT и Supabase-секреты;
- не используйте `service_role` в Android-приложении;
- сохраняйте RLS включённым для пользовательских таблиц;
- проверяйте владение сессией внутри `security definer` RPC;
- перед изменением self-hosted Supabase создавайте резервную копию базы и конфигурации;
- не запускайте `docker compose down -v` для рабочей базы.
