# Analytics Specification

## 1. Правила

- Названия событий в `snake_case`.
- Не отправлять email, JWT, текст заметок и другие чувствительные данные.
- Для каждого события фиксировать `app_version`, `platform`, `timezone`.
- Не использовать свободный текст как property без необходимости.
- События должны иметь стабильную схему.

## 2. Основные события

### Авторизация

`sign_up_started`

`sign_up_completed`

Properties:
- method.

`login_completed`

`logout_completed`

### Онбординг

`onboarding_started`

`onboarding_step_completed`

Properties:
- step;
- step_index.

`onboarding_completed`

Properties:
- selected_goal;
- current_rank;
- daily_minutes;
- reminder_enabled.

### Главный экран

`home_viewed`

`daily_plan_opened`

Properties:
- plan_date;
- exercise_count;
- estimated_minutes.

### Тренировка

`training_started`

Properties:
- session_id;
- plan_id;
- exercise_count.

`exercise_started`

Properties:
- session_id;
- exercise_id;
- category;
- position.

`exercise_completed`

Properties:
- exercise_id;
- category;
- duration_seconds;
- result_type.

`training_completed`

Properties:
- session_id;
- duration_seconds;
- exercise_completed_count;
- awarded_xp;
- streak_after.

`training_abandoned`

Properties:
- session_id;
- last_position;
- duration_seconds.

### Прогресс

`progress_viewed`

`achievement_unlocked_viewed`

Properties:
- achievement_id.

### Уведомления

`notification_permission_requested`

`notification_permission_result`

Properties:
- granted.

`notification_opened`

Properties:
- notification_type.

## 3. Воронки

### Activation

```text
sign_up_completed
-> onboarding_completed
-> daily_plan_opened
-> training_started
-> training_completed
```

### Habit

```text
first training_completed
-> second training_completed within 48h
-> third training_completed within 7d
```

## 4. Метрики

- Sign-up completion rate.
- Onboarding completion rate.
- First training completion rate.
- D1 retention.
- D7 retention.
- Three-trainings-in-seven-days rate.
- Training completion rate.
- Median training duration.
- Notification opt-in rate.
- Crash-free users.
