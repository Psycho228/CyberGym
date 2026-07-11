# KODA.md — Контекст проекта CyberGym (NextRank)

## Обзор проекта

**NextRank** — мобильное Android-приложение формата «Duolingo для киберспорта». Пользователь ежедневно получает короткую тренировочную программу по CS2, выполняет задания, зарабатывает XP, поддерживает streak и отслеживает прогресс. Проект находится на ранней стадии разработки: создан каркас многосессионного Gradle-проекта, реализованы базовые экраны UI и навигация, но интеграция с Supabase SDK ещё не завершена.

### Назначение

Помочь игрокам CS2 регулярно развивать игровые навыки (aim, movement, spray control, game sense) через короткие ежедневные тренировки с системой прогрессии и геймификации.

### Целевая платформа

- Android 9+ (minSdk 28, targetSdk 36)
- Только Kotlin
- Portrait-first
- Интерфейс на русском, строки готовы к локализации

### Основные технологии

| Категория | Технология |
|---|---|
| Язык | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Навигация | Navigation Compose |
| DI | Koin |
| Сеть/Бэкенд | Self-hosted Supabase (PostgreSQL, Auth, RLS, RPC, Edge Functions) |
| Сериализация | Kotlinx Serialization |
| Изображения | Coil 3 |
| Локальное хранилище | DataStore Preferences |
| Фоновые задачи | WorkManager |
| Тестирование | JUnit, Turbine, MockK, Compose UI Test |
| Качество кода | Detekt, Ktlint, Android Lint |
| Сборка | Gradle Kotlin DSL, Version Catalog |

## Архитектура

### Многосессионная структура (feature-first)

```
:app                          — точка входа, навигация, DI-конфигурация
:core:common                  — Result, AppError, модели, Clock
:core:designsystem            — тема, типографика, компоненты
:core:navigation              — маршруты и граф навигации
:core:network                 — Supabase клиент, DTO, мапперы
:core:analytics               — интерфейс аналитики и NoOp-реализация
:feature:auth                 — вход, регистрация, сессия
:feature:onboarding           — онбординг (никнейм, ранг, цель, время)
:feature:home                 — главный экран (тренировка дня, XP, streak)
:feature:training             — список упражнений, сессия тренировки
:feature:progress             — статистика, календарь, история
:feature:profile              — профиль игрока
```

### Слои внутри feature-модулей

```
feature/<name>/
  data/         — Supabase DTO, мапперы, реализации repository
  domain/       — модели предметной области, интерфейсы repository
  presentation/ — Compose-экраны, ViewModel, UI state, UI events
  di/           — Koin-модули
```

### Поток данных

```
Compose Screen → ViewModel (StateFlow<UiState>) → Repository (интерфейс)
  → Repository implementation → Supabase client / RPC
```

### Навигация

```
Splash → Auth (Login / Register) → Onboarding → Main
  ├─ Home
  ├─ Training / TrainingSession
  ├─ Progress
  └─ Profile
```

### Принципы архитектуры

- UI не связан напрямую с Supabase SDK — все внешние источники скрыты за repository-интерфейсами.
- DTO из data-слоя не передаются в presentation — используются отдельные UI-модели.
- Каждый экран имеет единый immutable UI state (`@Immutable data class`).
- События пользователя оформляются как sealed interface.
- Одноразовые эффекты — через Channel или SharedFlow.
- Все операции с XP, достижениями и streak должны быть идемпотентными.
- Прямая запись клиента в поля `xp`, `level`, `streak` запрещена — только серверные RPC.

## Сборка и запуск

### Команды Gradle

```bash
# Сборка debug APK
./gradlew assembleDebug

# Сборка release APK
./gradlew assembleRelease

# Проверки качества кода
./gradlew ktlintCheck
./gradlew detekt
./gradlew lint

# Модульные тесты
./gradlew test

# Инструментальные тесты (требуется эмулятор/устройство)
./gradlew connectedAndroidTest
```

### CI-пайплайн (на pull request)

```bash
./gradlew ktlintCheck
./gradlew detekt
./gradlew lint
./gradlew test
./gradlew assembleDebug
```

### Конфигурация Supabase

Секреты передаются через `local.properties` (не в git):

```properties
supabase_url=https://your-project.supabase.co
supabase_anon_key=your-anon-key-here
```

Шаблон: `local.properties.template`. Значения попадают в `BuildConfig` через `buildConfigField` в `app/build.gradle.kts`.

> **Важно:** Service role key никогда не должен попадать в Android-приложение.

### Build types

- `debug` — без минификации
- `release` — ProGuard/R8 минификация и сжатие ресурсов

## База данных (Supabase / PostgreSQL)

### Основные таблицы

| Таблица | Назначение |
|---|---|
| `profiles` | Профиль игрока (nickname, timezone, rank, XP, level, streak) |
| `games` | Справочник игр (MVP — только CS2) |
| `exercise_categories` | Категории упражнений (warmup, aim, spray, movement и др.) |
| `exercises` | Контент упражнений |
| `training_plan_templates` | Шаблоны программ |
| `training_plan_template_items` | Упражнения внутри шаблона |
| `daily_plans` | Программа пользователя на конкретную дату |
| `daily_plan_items` | Упражнения конкретной программы |
| `training_sessions` | Прохождение тренировки |
| `exercise_results` | Результаты упражнений |
| `xp_transactions` | Ledger начислений XP |
| `achievements` | Справочник достижений |
| `user_achievements` | Открытые достижения |
| `push_tokens` | Токены устройств для push-уведомлений |

### Безопасность

- RLS включён на всех пользовательских таблицах.
- Пользователь может читать/изменять только собственные данные.
- Прямое изменение `total_xp`, `level`, `streak` запрещено — только через RPC.
- Критические операции выполняются транзакционно в PostgreSQL RPC с проверкой `auth.uid()`.

### Ключевые RPC

```sql
get_or_create_daily_plan(target_date date)
complete_training_session(p_session_id uuid, p_idempotency_key uuid, p_client_completed_at timestamptz, p_results jsonb)
get_progress_summary()
delete_my_account()
```

### Стартовая схема

Полная SQL-схема находится в `nextrank-agent-pack/schema.sql` — включает создание таблиц, индексов, RLS-политик и seed-данных.

## Правила разработки

### Кодирование

- Только Kotlin. UI — только Jetpack Compose + Material 3.
- Код должен компилироваться после каждого завершённого шага.
- Не оставлять мёртвый код, закомментированные блоки и временные обходы без TODO.
- Все TODO должны содержать причину и ожидаемое условие удаления.
- Не добавлять библиотеки «на будущее» — любая зависимость должна решать конкретную задачу.
- Не использовать Room до появления подтверждённой offline-задачи.
- Не показывать пользователю сырые exception messages — маппить в понятные типы через `AppError`.
- Не скрывать исключения пустыми `catch`.

### Стиль кода

- Kotlin code style: `official` (установлено в `gradle.properties`).
- Статический анализ: Detekt (конфиг в `detekt-config.yml`), Ktlint, Android Lint.
- Gradle Kotlin DSL и Version Catalog (`gradle/libs.versions.toml`) для управления зависимостями.

### Тестирование

- ViewModel-тесты с Turbine для проверки StateFlow.
- MockK или простые fake-реализации для repository.
- Compose UI Test для ключевых экранов.
- Integration-тесты: RLS, идемпотентность RPC, миграции.
- Каждый экран UI должен иметь состояния: loading, content, error.

### Git

Формат веток:
```
feat/NR-001-auth
fix/NR-014-streak-timezone
docs/NR-020-analytics
```

Формат commit message:
```
feat(training): add daily session screen
fix(progress): prevent duplicate xp transaction
```

Один commit — одно логическое изменение.

### Запрещено без отдельного ADR

Spring Boot, Ktor backend, микросервисы, Kafka, Redis, Kubernetes, GraphQL, Room, собственный design system framework, сложная многомодульность с десятками Gradle-модулей, event sourcing, CQRS.

### Definition of Done

Задача завершена, если:
- код компилируется;
- добавлены необходимые тесты;
- нет новых lint errors;
- UI имеет loading, content и error состояния;
- учтена доступность;
- обновлена документация;
- миграции обратимы либо описана стратегия rollback;
- секреты не добавлены;
- acceptance criteria выполнены.

## Текущее состояние реализации

### Что готово

- Каркас многосессионного Gradle-проекта с Version Catalog.
- Модули: `app`, `core` (common, designsystem, navigation, network, analytics), `feature` (auth, onboarding, home, training, progress, profile).
- Базовая навигация (Splash → Login → Register → Onboarding → Home → Training/Progress/Profile).
- Экраны: Login, Register, Onboarding, Home, Training, TrainingSession, Progress, Profile.
- ViewModel для auth и home.
- Абстракции: `ApiResult`, `AppError`, `Analytics`, `Clock`, `Route`/`NavGraph`.
- Koin DI-конфигурация с модулями для всех feature.
- SQL-схема базы данных с RLS-политиками и seed-данными.
- Полная продуктовая и техническая документация.

### Что не готово / в процессе

- Supabase SDK не подключён (`SupabaseClientFactory` содержит `TODO()`).
- `HomeViewModel` использует заглушечные данные вместо repository.
- Многие экраны — каркасные (Training, Progress, Profile).
- RPC для daily plan и complete_training_session не реализованы.
- Система XP, streak и достижений не реализована.
- Push-уведомления не настроены.
- Тесты отсутствуют (только шаблонные `ExampleUnitTest` и `ExampleInstrumentedTest`).

## Документация

Вся проектная документация находится в `nextrank-agent-pack/`:

| Файл | Содержание |
|---|---|
| `AGENTS.md` | Главные инструкции для AI-агента: правила, стек, архитектура, Definition of Done |
| `PRD.md` | Продуктовые требования: сценарии, метрики, acceptance criteria |
| `TDD.md` | Технический дизайн: модули, потоки данных, безопасность, тестирование, CI |
| `DATABASE.md` | Модель данных, RLS, RPC, индексы, миграции |
| `schema.sql` | Стартовая SQL-схема PostgreSQL с таблицами, политиками и seed-данными |
| `BACKLOG.md` | Приоритизированный backlog (NR-001 … NR-100, P0/P1/P2) |
| `ANALYTICS.md` | События аналитики, воронки, метрики |
| `app-structure.txt` | Рекомендуемая структура модулей и пакетов |
| `ADR/0001-kotlin-compose.md` | Решение по стеку: Kotlin + Compose |
| `ADR/0002-supabase-direct.md` | Решение: прямой доступ к Supabase для MVP |

## Порядок реализации (из BACKLOG.md)

1. Каркас проекта (Epic 0) — частично выполнен
2. Авторизация (Epic 1)
3. Онбординг и профиль (Epic 2)
4. Главный экран (Epic 3)
5. Контент тренировок (Epic 4)
6. Ежедневная программа (Epic 5)
7. Прохождение тренировки (Epic 6)
8. Безопасное начисление XP (Epic 7)
9. Streak (Epic 7)
10. Прогресс и статистика (Epic 8)
11. Достижения (Epic 9)
12. Уведомления (Epic 10)
13. Аналитика (Epic 11)
14. Hardening и закрытый тест (Epic 11)
