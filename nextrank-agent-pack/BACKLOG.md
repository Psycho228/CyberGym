# Product Backlog

Приоритеты:

- P0 — необходимо для MVP.
- P1 — важно для закрытого теста.
- P2 — после подтверждения базовой гипотезы.

## Epic 0. Foundation

- [ ] `NR-001` P0 Создать Android-проект на Kotlin и Compose.
- [ ] `NR-002` P0 Настроить Gradle Kotlin DSL и Version Catalog.
- [ ] `NR-003` P0 Добавить Koin.
- [ ] `NR-004` P0 Настроить Navigation Compose.
- [ ] `NR-005` P0 Настроить Material 3 theme.
- [ ] `NR-006` P0 Добавить базовые CI checks.
- [ ] `NR-007` P0 Добавить Detekt, Ktlint и Android Lint.
- [ ] `NR-008` P0 Создать build types debug/staging/release.
- [ ] `NR-009` P0 Создать конфигурацию Supabase endpoint и anon key.
- [ ] `NR-010` P0 Добавить единый Result/Error abstraction.

## Epic 1. Authentication

- [ ] `NR-011` P0 Экран входа.
- [ ] `NR-012` P0 Экран регистрации.
- [ ] `NR-013` P0 Email/password sign-up.
- [ ] `NR-014` P0 Login.
- [ ] `NR-015` P0 Session restore.
- [ ] `NR-016` P0 Logout.
- [ ] `NR-017` P1 Password reset.
- [ ] `NR-018` P1 Account deletion.
- [ ] `NR-019` P0 Auth ViewModel tests.
- [ ] `NR-020` P0 Ошибки и retry.

## Epic 2. Onboarding

- [ ] `NR-021` P0 Никнейм.
- [ ] `NR-022` P0 Выбор CS2 rank.
- [ ] `NR-023` P0 Выбор цели.
- [ ] `NR-024` P0 Выбор daily minutes.
- [ ] `NR-025` P1 Выбор reminder time.
- [ ] `NR-026` P0 Сохранение timezone.
- [ ] `NR-027` P0 Завершение onboarding.
- [ ] `NR-028` P0 Восстановление шага после process death.
- [ ] `NR-029` P0 Onboarding UI tests.

## Epic 3. Home

- [ ] `NR-030` P0 Главный экран.
- [ ] `NR-031` P0 Карточка тренировки дня.
- [ ] `NR-032` P0 Отображение XP и уровня.
- [ ] `NR-033` P0 Отображение streak.
- [ ] `NR-034` P0 Loading skeleton.
- [ ] `NR-035` P0 Error state и retry.
- [ ] `NR-036` P1 Empty state без программы.

## Epic 4. Training Content

- [ ] `NR-037` P0 Таблицы exercises и categories.
- [ ] `NR-038` P0 Seed для CS2.
- [ ] `NR-039` P0 Репозиторий контента.
- [ ] `NR-040` P1 Media loading через Coil.
- [ ] `NR-041` P1 Signed URLs для private assets.
- [ ] `NR-042` P0 Admin workflow наполнения через Supabase.

## Epic 5. Daily Plan

- [ ] `NR-043` P0 Таблицы templates и daily plans.
- [ ] `NR-044` P0 RPC get_or_create_daily_plan.
- [ ] `NR-045` P0 Идемпотентность генерации программы.
- [ ] `NR-046` P0 Экран списка упражнений.
- [ ] `NR-047` P0 Estimate duration.
- [ ] `NR-048` P0 Unit и integration tests для RPC.

## Epic 6. Training Session

- [ ] `NR-049` P0 Создание session.
- [ ] `NR-050` P0 Timer exercise.
- [ ] `NR-051` P0 Repetitions exercise.
- [ ] `NR-052` P0 Score exercise.
- [ ] `NR-053` P0 Checklist exercise.
- [ ] `NR-054` P0 External task exercise.
- [ ] `NR-055` P0 Self-rating exercise.
- [ ] `NR-056` P0 Навигация вперед/назад.
- [ ] `NR-057` P0 Сохранение draft results.
- [ ] `NR-058` P1 Resume interrupted session.
- [ ] `NR-059` P0 Completion summary.

## Epic 7. XP and Streak

- [ ] `NR-060` P0 RPC complete_training_session.
- [ ] `NR-061` P0 XP ledger.
- [ ] `NR-062` P0 Защита от duplicate XP.
- [ ] `NR-063` P0 Server-side level calculation.
- [ ] `NR-064` P0 Server-side timezone streak.
- [ ] `NR-065` P0 Транзакционное завершение session.
- [ ] `NR-066` P0 RLS tests.
- [ ] `NR-067` P0 Повтор запроса с тем же idempotency key.
- [ ] `NR-068` P1 Retry через WorkManager.

## Epic 8. Progress

- [ ] `NR-069` P0 Progress summary query.
- [ ] `NR-070` P0 Экран прогресса.
- [ ] `NR-071` P0 Календарь активности.
- [ ] `NR-072` P0 Последние сессии.
- [ ] `NR-073` P1 График по категориям.
- [ ] `NR-074` P1 Personal best.
- [ ] `NR-075` P0 Pull-to-refresh.

## Epic 9. Achievements

- [ ] `NR-076` P1 Таблицы achievements.
- [ ] `NR-077` P1 Seed достижений.
- [ ] `NR-078` P1 Server-side unlock.
- [ ] `NR-079` P1 Экран достижений.
- [ ] `NR-080` P1 Achievement dialog.

## Epic 10. Notifications

- [ ] `NR-081` P1 Permission flow.
- [ ] `NR-082` P1 Регистрация push token.
- [ ] `NR-083` P1 Daily reminder.
- [ ] `NR-084` P1 Streak risk notification.
- [ ] `NR-085` P1 Deep links.
- [ ] `NR-086` P1 Token refresh.

## Epic 11. Analytics and Quality

- [ ] `NR-087` P0 Analytics abstraction.
- [ ] `NR-088` P0 Activation events.
- [ ] `NR-089` P0 Training events.
- [ ] `NR-090` P1 Notification events.
- [ ] `NR-091` P0 Crash reporting.
- [ ] `NR-092` P0 Privacy review.
- [ ] `NR-093` P0 Accessibility review.
- [ ] `NR-094` P0 Performance smoke test.
- [ ] `NR-095` P0 Closed testing release checklist.

## Epic 12. Later

- [ ] `NR-096` P2 Premium subscriptions.
- [ ] `NR-097` P2 AI recommendations.
- [ ] `NR-098` P2 Match/demo analysis.
- [ ] `NR-099` P2 Valorant support.
- [ ] `NR-100` P2 iOS client.
