# Technical Design Document

## 1. Обзор

Android-приложение обращается напрямую к self-hosted Supabase через Kotlin SDK. Привилегированная логика выполняется в PostgreSQL RPC или Edge Functions.

```text
Android App
  |
  | HTTPS + Supabase anon key + user JWT
  v
Supabase
  |- Auth
  |- PostgREST
  |- PostgreSQL
  |- Realtime (не обязателен для MVP)
  |- Edge Functions
  v
S3-compatible Object Storage
```

## 2. Архитектурные цели

- быстрый выпуск MVP;
- минимальная инфраструктура;
- четкие границы между UI, domain и data;
- возможность заменить Supabase repository на API repository;
- безопасность пользовательских данных;
- идемпотентность наград;
- тестируемость бизнес-логики.

## 3. Android-модули

Начать с ограниченной модульности:

```text
:app
:core:common
:core:designsystem
:core:navigation
:core:network
:core:analytics
:feature:auth
:feature:onboarding
:feature:home
:feature:training
:feature:progress
:feature:profile
```

Достижения и настройки можно сначала держать внутри соответствующих feature и выделить позже.

## 4. Навигация

Основной graph:

```text
Splash
 ├─ Auth
 └─ Onboarding
      └─ Main
          ├─ Home
          ├─ Training
          ├─ Progress
          └─ Profile
```

Навигационные маршруты не должны содержать сложные модели. Передавать только идентификаторы и простые значения.

## 5. DI

Koin используется для:

- Supabase client;
- repositories;
- data sources;
- analytics;
- ViewModels;
- dispatcher provider;
- clock abstraction.

## 6. Потоки данных

```text
Compose Screen
  -> ViewModel
  -> Repository interface
  -> Repository implementation
  -> Supabase client / RPC
```

ViewModel публикует `StateFlow<UiState>`.

Одноразовые события:
- Snackbar;
- navigation;
- external link.

Использовать `Channel` или `SharedFlow` только для действительно одноразовых эффектов.

## 7. Модели

Разделять:

- DTO;
- domain model;
- UI model.

Не использовать Supabase DTO в presentation.

## 8. Авторизация

Поддержать:

- email/password;
- refresh session;
- logout;
- password reset;
- account deletion.

При первом входе создается `profiles` row через безопасный database trigger или RPC.

## 9. Ежедневная программа

Алгоритм MVP:

1. Клиент запрашивает программу текущего дня.
2. Если программа уже существует, сервер возвращает ее.
3. Если нет, RPC создает программу из активного шаблона.
4. Результат детерминирован на один день и пользователя.
5. Повторный запрос возвращает ту же программу.

Пример RPC:

```text
get_or_create_daily_plan(target_date)
```

## 10. Завершение тренировки

Клиент отправляет:

- session_id;
- exercise results;
- client_completed_at;
- idempotency_key.

Сервер:

1. проверяет владельца;
2. проверяет обязательные упражнения;
3. фиксирует завершение;
4. создает XP transaction;
5. обновляет агрегированный профиль;
6. обновляет streak;
7. проверяет достижения;
8. возвращает итоговый summary.

Операция выполняется транзакционно.

## 11. Время и timezone

- В БД хранить UTC timestamps.
- В профиле хранить IANA timezone.
- Для streak использовать локальную дату пользователя, вычисленную сервером.
- Не полагаться на локальные часы клиента при начислении наград.
- Клиентское время использовать только как диагностическое поле.

## 12. Хранилище

S3 используется для:

```text
public/
  exercise-images/
  training-icons/
  achievement-icons/

private/
  avatars/
  future/demo-files/
```

Для private assets использовать signed URL.

## 13. Offline

MVP не является offline-first.

Допускается:

- кеш текущего UI state в памяти;
- DataStore для настроек;
- кеш Coil;
- повтор отправки незавершенного запроса через WorkManager.

Room не использовать до появления подтвержденных offline use cases.

## 14. WorkManager

Применение:

- повтор отправки аналитики;
- повтор синхронизации результатов;
- обновление push token;
- фоновые задачи, переживающие перезапуск процесса.

Не использовать WorkManager как обычный таймер UI.

## 15. Аналитика

Интерфейс:

```kotlin
interface Analytics {
    fun track(event: AnalyticsEvent)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
}
```

Реализация заменяема.

## 16. Конфигурация

Build types:

- debug;
- staging;
- release.

Секреты не хранить в git.

Публичные endpoint и anon key допускается передавать через:
- local properties;
- CI secrets;
- generated BuildConfig.

## 17. Логирование

- Timber или простая абстракция Logger;
- в release не логировать JWT, email, profile payload;
- сетевые body logs отключены в release;
- ошибки должны иметь correlation id, если сервер его возвращает.

## 18. Безопасность

- RLS на всех пользовательских таблицах;
- запрет прямого изменения XP;
- проверка ownership внутри RPC;
- rate limiting для Edge Functions;
- минимальные bucket policies;
- account deletion workflow;
- data export позже.

## 19. Тестирование

### Unit

- ViewModel;
- mapper;
- XP summary parser;
- streak date rules;
- repository fake contracts.

### Integration

- Supabase staging;
- RLS tests;
- RPC idempotency;
- migration smoke tests.

### UI

- auth flow;
- onboarding;
- complete training;
- error and retry;
- progress refresh.

## 20. CI

На pull request:

```text
./gradlew ktlintCheck
./gradlew detekt
./gradlew lint
./gradlew test
./gradlew assembleDebug
```

На main:

- сборка staging APK;
- публикация артефакта;
- миграции выполняются отдельным контролируемым job.
