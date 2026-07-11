# Database Design

## 1. Принципы

- UUID как primary key.
- `timestamptz` для времени.
- UTC для хранения.
- IANA timezone в профиле.
- RLS включен по умолчанию.
- Награды создаются только серверной функцией.
- Все критические операции идемпотентны.
- Агрегаты можно восстановить из событий и сессий.

## 2. Основные таблицы

### profiles

Публично-прикладной профиль пользователя.

Поля:

- id;
- nickname;
- timezone;
- current_rank;
- primary_goal;
- daily_minutes;
- reminder_time;
- total_xp;
- level;
- current_streak;
- longest_streak;
- last_training_local_date;
- onboarding_completed.

### games

Справочник игр. MVP содержит только CS2.

### exercise_categories

Категории упражнений.

### exercises

Контент упражнений.

### training_plan_templates

Шаблоны программ.

### training_plan_template_items

Упражнения внутри шаблона.

### daily_plans

Конкретная программа пользователя на дату.

### daily_plan_items

Конкретные упражнения программы.

### training_sessions

Прохождение программы.

### exercise_results

Результаты упражнений.

### xp_transactions

Ledger начислений XP.

### achievements

Справочник достижений.

### user_achievements

Открытые достижения.

### push_tokens

Токены устройств.

## 3. Критические ограничения

- `daily_plans`: unique `(user_id, plan_date)`.
- `training_sessions`: один завершенный session для daily plan.
- `xp_transactions`: unique `(user_id, source_type, source_id)`.
- `user_achievements`: unique `(user_id, achievement_id)`.
- `push_tokens.token`: unique.
- Нельзя завершить чужую сессию.
- Нельзя изменить finalized result.
- Нельзя напрямую обновить `profiles.total_xp`.

## 4. RLS

Пользователь может:

- читать и обновлять разрешенные поля собственного профиля;
- читать активный публичный контент;
- читать свои daily plans;
- создавать и читать свои draft session results;
- читать собственный прогресс;
- регистрировать собственный push token.

Пользователь не может:

- читать чужие session;
- назначать себе XP;
- открывать achievement;
- редактировать training templates;
- менять системные поля прогресса.

## 5. RPC

Рекомендуемые функции:

```text
get_or_create_daily_plan(target_date date)
complete_training_session(
  p_session_id uuid,
  p_idempotency_key uuid,
  p_client_completed_at timestamptz,
  p_results jsonb
)
get_progress_summary()
delete_my_account()
```

Все функции должны использовать `auth.uid()`.

## 6. Индексы

Минимум:

- `daily_plans(user_id, plan_date)`;
- `training_sessions(user_id, completed_at desc)`;
- `exercise_results(session_id)`;
- `xp_transactions(user_id, created_at desc)`;
- `user_achievements(user_id, unlocked_at desc)`;
- `exercises(game_id, category_id, is_active)`.

## 7. Миграции

- каждая миграция имеет номер и описание;
- миграции не редактируются после применения;
- destructive change выполняется в несколько фаз;
- production migration не должна зависеть от Android release;
- rollback strategy описывается в PR.
