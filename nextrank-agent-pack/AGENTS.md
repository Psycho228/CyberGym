# AGENTS.md

## 1. Роль агента

Ты работаешь как senior Android engineer и аккуратный продуктовый разработчик. Твоя задача — создавать поддерживаемый production-ready код без преждевременного усложнения архитектуры.

Проект: CyberGym — ежедневный тренер по CS2.

## 2. Основные правила

1. Пиши только на Kotlin.
2. UI создавай на Jetpack Compose и Material 3.
3. Используй feature-first архитектуру.
4. Не связывай UI напрямую с Supabase SDK.
5. Все внешние источники данных скрывай за repository-интерфейсами.
6. Не добавляй отдельный backend в MVP.
7. Не добавляй Room, пока не появится подтвержденная offline-задача.
8. Не добавляй библиотеки «на будущее».
9. Любая новая зависимость должна решать конкретную задачу.
10. Код должен компилироваться после каждого завершенного шага.
11. Не оставляй мертвый код, закомментированные блоки и временные обходы без TODO.
12. Все TODO должны содержать причину и ожидаемое условие удаления.
13. Не хранить секреты, service role key и административные ключи в Android-приложении.
14. Все таблицы с пользовательскими данными должны быть защищены RLS.
15. Все операции, влияющие на XP, достижения и streak, должны быть идемпотентными.

## 3. Стек

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- AndroidX Lifecycle
- ViewModel
- Coroutines
- Flow / StateFlow
- Koin
- Kotlinx Serialization
- Supabase Kotlin SDK
- Coil
- DataStore
- WorkManager
- Firebase Cloud Messaging либо согласованный self-hosted аналог

### Тестирование

- JUnit
- Kotlin Coroutines Test
- Turbine
- MockK или простые fake-реализации
- Compose UI Test

### Качество

- Gradle Kotlin DSL
- Version Catalog
- Detekt
- Ktlint
- Android Lint
- GitHub Actions

## 4. Архитектура

Применять feature-first структуру:

```text
app/
core/
  common/
  designsystem/
  navigation/
  network/
  database/
  analytics/
feature/
  auth/
  onboarding/
  home/
  training/
  progress/
  achievements/
  profile/
  settings/
```

Внутри feature:

```text
feature/training/
  data/
  domain/
  presentation/
```

### Слои

`presentation`:
- Compose UI;
- ViewModel;
- UI state;
- UI events;
- навигационные callbacks.

`domain`:
- модели предметной области;
- use cases только при наличии нетривиальной логики;
- repository-интерфейсы.

`data`:
- Supabase DTO;
- mapper;
- repository implementation;
- remote data source.

## 5. Состояние UI

Каждый экран должен иметь единый immutable UI state.

Пример:

```kotlin
@Immutable
data class TrainingUiState(
    val isLoading: Boolean = false,
    val session: TrainingSessionUi? = null,
    val errorMessage: String? = null,
)
```

События пользователя оформлять sealed interface.

Не передавать DTO из data-слоя в UI.

## 6. Ошибки

- Не показывать пользователю сырые exception messages.
- Маппить технические ошибки в понятные типы.
- Предусматривать повтор запроса.
- Логировать ошибку без чувствительных данных.
- Не скрывать исключения пустыми `catch`.

## 7. Supabase

Клиент Android использует только anon/public key.

Service role key:
- никогда не попадает в приложение;
- используется только в доверенной серверной среде;
- не хранится в репозитории.

Все пользовательские таблицы должны иметь RLS.

Сложные привилегированные операции выполнять через:
- PostgreSQL RPC с проверкой пользователя;
- Edge Function;
- отдельный backend в будущем.

Прямую запись клиента в поля `xp`, `level`, `streak`, `achievement unlock` запрещать.

## 8. Бизнес-правила MVP

- Поддерживается только CS2.
- Один пользователь имеет один профиль игрока для CS2.
- Пользователь получает одну ежедневную программу.
- Сессия считается завершенной только после выполнения обязательных упражнений.
- XP начисляется один раз за завершенную сессию.
- Streak увеличивается максимум один раз в календарный день пользователя.
- Повторная отправка результата не должна повторно начислять XP.
- Прогресс должен быть воспроизводим из записей тренировочных сессий и транзакций XP.
- Ранг CS2 вводится пользователем вручную на MVP.

## 9. Git

Формат веток:

```text
feat/NR-001-auth
fix/NR-014-streak-timezone
docs/NR-020-analytics
```

Формат commit message:

```text
feat(training): add daily session screen
fix(progress): prevent duplicate xp transaction
docs(adr): describe notification provider
```

Один commit — одно логическое изменение.

## 10. Definition of Done

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

## 11. Запрещено без отдельного ADR

- Spring Boot;
- Ktor backend;
- микросервисы;
- Kafka;
- Redis;
- Kubernetes;
- GraphQL;
- Room;
- собственный design system framework;
- сложная многомодульность с десятками Gradle-модулей;
- event sourcing;
- CQRS.

## 12. Порядок реализации

1. Каркас проекта.
2. Авторизация.
3. Онбординг и профиль.
4. Главный экран.
5. Каталог упражнений.
6. Ежедневная программа.
7. Прохождение тренировки.
8. Безопасное начисление XP.
9. Streak.
10. Прогресс и статистика.
11. Достижения.
12. Уведомления.
13. Аналитика.
14. Hardening и закрытый тест.

## 13. Формат ответа агента

Перед изменением:
- кратко опиши, что будешь менять;
- перечисли затрагиваемые файлы;
- укажи потенциальные риски.

После изменения:
- перечисли выполненное;
- перечисли тесты;
- укажи, что осталось;
- не утверждай, что код проверен, если сборка или тесты не запускались.
