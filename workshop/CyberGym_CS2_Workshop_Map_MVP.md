# CyberGym CS2 Workshop Map MVP

## 1. Цель

Сделать тренировочную CS2 workshop-карту, которая работает как офлайн-тренажёр для CyberGym:

1. приложение назначает пользователю конкретные упражнения;
2. пользователь запускает карту в CS2;
3. проходит нужные станции;
4. карта показывает результат в формате короткого кода;
5. пользователь вводит код в CyberGym;
6. приложение сохраняет результат, начисляет XP и адаптирует следующий план.

MVP не требует автоматической синхронизации из CS2 в приложение. Это резко снижает сложность и позволяет быстро проверить продуктовую идею.

## 2. Принцип MVP

Карта должна быть одной тренировочной ареной с несколькими станциями.

Рабочий сценарий:

```text
CyberGym показывает тренировку:
  AIM100 — убить 100 целей
  SPRAY30 — 30 секунд spray control
  STRAFE50 — 50 counter-strafe дуэлей

Игрок открывает карту CyberGym Training Hub в CS2.
Проходит станции.
Карта показывает результат:
  AIM100:T42.8:A83:HS41

Игрок вводит код в CyberGym.
Приложение парсит результат и сохраняет прогресс.
```

## 3. Сложность

Оценка сложности:

| Версия | Сложность | Что входит |
| --- | ---: | --- |
| MVP с ручным вводом результата | 5/10 | карта, станции, result code, ввод в приложении |
| Хорошая v1 | 7/10 | больше станций, нормальный UI внутри карты, баланс scoring |
| Автосинхронизация | 9/10 | companion app, парсинг логов/демок или внешний мост |

Главный риск — не Android и не Supabase, а разработка и тестирование самой карты в CS2 Workshop Tools/Hammer.

## 4. Карта: CyberGym Training Hub

Рабочее название:

```text
workshop_cybergym_training_hub
```

Структура:

```text
Spawn / Hub
├── Aim Station
├── Flick Station
├── Spray Station
├── Counter-strafe Station
├── Prefire Station
├── Movement Station
└── Result / Help Wall
```

### 4.1 Hub

В центре карты:

- spawn игрока;
- стена с названием CyberGym;
- короткая инструкция;
- кнопки/порталы выбора станции;
- место, где отображается последний result code;
- ссылка на приложение/QR-код на сайт проекта, если появится landing.

Требования:

- быстрый доступ ко всем станциям;
- никакой лишней ходьбы;
- визуальный стиль: неон, чистая сетка, киберспортивный тренировочный центр;
- высокая читаемость текста.

## 5. Станции MVP

### 5.1 AIM100 — Aim 100 Bots

Цель: базовая скорость и точность наведения.

Сценарий:

1. Игрок нажимает Start.
2. На арене появляются боты/мишени.
3. Нужно сделать 100 kills.
4. Карта считает время, выстрелы, попадания, headshots.
5. После завершения показывает код.

Метрики:

- `time_seconds`;
- `kills`;
- `shots`;
- `hits`;
- `accuracy_percent`;
- `headshots`;
- `headshot_percent`;

Код:

```text
AIM100:T42.8:A83:HS41
```

Расшифровка:

- `AIM100` — slug упражнения;
- `T42.8` — время 42.8 секунды;
- `A83` — accuracy 83%;
- `HS41` — 41 headshot.

### 5.2 FLICK30 — Flick Reaction

Цель: реакция и резкий перевод прицела.

Сценарий:

1. Игрок стоит в центре.
2. Мишени появляются случайно слева/справа/сверху/снизу.
3. Нужно уничтожить 30 целей.
4. Ошибочные выстрелы снижают score.

Метрики:

- `time_seconds`;
- `targets`;
- `hits`;
- `misses`;
- `average_reaction_ms`, если возможно реализовать;
- `score`.

Код:

```text
FLICK30:T31.4:H30:M7:S820
```

### 5.3 SPRAY30 — Spray Control

Цель: контроль спрея.

Сценарий:

1. Игрок выбирает оружие: AK-47 / M4A1-S / M4A4.
2. На стене или группе целей начинается 30-секундный тест.
3. Карта считает попадания в целевую область.

Метрики:

- `weapon`;
- `duration_seconds`;
- `shots`;
- `hits`;
- `accuracy_percent`;
- `score`.

Код:

```text
SPRAY30:AK:A61:H74:S610
```

### 5.4 STRAFE50 — Counter-strafe Timing

Цель: остановка перед выстрелом.

Сценарий:

1. Игрок двигается влево/вправо.
2. Цель появляется после сигнала.
3. Нужно остановиться и попасть.
4. Идеально: карта штрафует выстрелы в движении.

Метрики:

- `attempts`;
- `hits`;
- `clean_hits`;
- `moving_shots`;
- `accuracy_percent`;
- `score`.

Код:

```text
STRAFE50:H43:C31:M9:S720
```

### 5.5 PREFIRE20 — Prefire Corners

Цель: отработка углов и crosshair placement.

Сценарий:

1. Мини-маршрут из 20 углов.
2. За каждым углом может быть бот.
3. Игрок должен проходить маршрут и чистить углы.

Метрики:

- `time_seconds`;
- `targets`;
- `kills`;
- `damage_taken`, если возможно;
- `score`.

Код:

```text
PREFIRE20:T58.2:K20:D17:S790
```

### 5.6 MOVE60 — Movement Course

Цель: базовое движение, стрейфы, прыжки, pathing.

Сценарий:

1. Короткая трасса на 60–90 секунд.
2. Чекпоинты.
3. Финиш показывает время.

Метрики:

- `time_seconds`;
- `checkpoints`;
- `fails`;
- `score`.

Код:

```text
MOVE60:T74.5:F3:S640
```

## 6. Result Code Format

Требования:

- короткий;
- вводится вручную;
- легко читается с экрана CS2;
- парсится в приложении;
- расширяемый;
- устойчивый к ошибкам.

Формат MVP:

```text
TASK:KEYVALUE:KEYVALUE:KEYVALUE
```

Примеры:

```text
AIM100:T42.8:A83:HS41
SPRAY30:AK:A61:H74:S610
STRAFE50:H43:C31:M9:S720
```

Ключи:

| Ключ | Значение |
| --- | --- |
| `T` | время в секундах |
| `A` | accuracy percent |
| `HS` | headshots или headshot percent, зависит от task |
| `H` | hits |
| `K` | kills |
| `M` | misses / moving shots |
| `C` | clean hits |
| `D` | damage taken |
| `F` | fails |
| `S` | score |

Для защиты от случайного ввода позже можно добавить checksum:

```text
AIM100:T42.8:A83:HS41:Z7K
```

В MVP checksum можно не делать.

## 7. Scoring

Приложение должно считать итоговый score независимо от карты, если возможно.

Карта показывает score для удобства, но CyberGym должен уметь пересчитать:

```text
score = base_score
  + speed_bonus
  + accuracy_bonus
  + headshot_bonus
  - penalty
```

Пример AIM100:

```text
base_score = 500
speed_bonus = max(0, 90 - time_seconds) * 5
accuracy_bonus = accuracy_percent * 3
headshot_bonus = headshots * 2
score = min(1000, calculated_score)
```

Score лучше хранить как `0..1000`.

## 8. Навыки CyberGym

Каждая станция мапится на навыки приложения:

| Task | Primary skill | Secondary skills |
| --- | --- | --- |
| AIM100 | aim | crosshair, discipline |
| FLICK30 | aim | reaction, crosshair |
| SPRAY30 | spray | discipline |
| STRAFE50 | movement | aim, counter-strafe |
| PREFIRE20 | positioning | crosshair, game sense |
| MOVE60 | movement | consistency |

Это позволит использовать результаты для персонального плана.

## 9. Изменения в Supabase

Нужны таблицы:

```sql
create table public.workshop_maps (
  id uuid primary key default gen_random_uuid(),
  slug text not null unique,
  title text not null,
  steam_workshop_url text,
  version text not null default '0.1.0',
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.workshop_tasks (
  id uuid primary key default gen_random_uuid(),
  map_id uuid not null references public.workshop_maps(id) on delete cascade,
  slug text not null unique,
  title text not null,
  description text not null,
  primary_skill text not null,
  secondary_skills text[] not null default '{}',
  result_schema jsonb not null default '{}'::jsonb,
  scoring_config jsonb not null default '{}'::jsonb,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.workshop_results (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  task_id uuid not null references public.workshop_tasks(id),
  result_code text not null,
  score integer check (score between 0 and 1000),
  time_seconds numeric,
  accuracy_percent numeric,
  kills integer,
  shots integer,
  hits integer,
  headshots integer,
  raw_result jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);
```

RLS:

- `workshop_maps` и `workshop_tasks` читают все authenticated;
- `workshop_results` пользователь читает и создаёт только свои.

## 10. Изменения в Android

Нужен новый flow:

### 10.1 В тренировке

Для упражнения типа `workshop_task` показывать:

- название станции;
- цель;
- как запустить карту;
- какие настройки выбрать;
- кнопку “Ввести результат”.

### 10.2 Экран ввода результата

Поля:

- `result_code`;
- preview распарсенного результата;
- кнопка “Сохранить”.

Ошибки:

- неизвестный task slug;
- неверный формат;
- значение вне диапазона;
- код уже сохранён, если добавим idempotency/checksum.

### 10.3 Профиль/Прогресс

Показывать:

- лучшие результаты по станциям;
- график AIM100 time;
- accuracy trend;
- последние workshop-тренировки.

## 11. Изменения в текущей тренировочной модели

В существующие `exercises` можно добавить:

```text
result_type = workshop_code
config = {
  "workshop_task_slug": "AIM100",
  "map_slug": "cybergym_training_hub"
}
```

Если `result_type` enum сейчас не содержит `workshop_code`, можно либо:

1. добавить новый enum value;
2. на MVP использовать существующий JSON/manual result.

Рекомендация для MVP: добавить отдельный результат `workshop_code`, потому что это станет центральной механикой CyberGym.

## 12. Hammer / CS2 Workshop Tools: ТЗ для mapper

Mapper должен собрать:

1. Hub.
2. Шесть изолированных станций.
3. Start/Reset кнопки для каждой станции.
4. Счётчики:
   - timer;
   - kills/hits;
   - attempts;
   - misses, если возможно;
   - score.
5. Result display:
   - крупный текст на стене;
   - формат `TASK:...`;
   - не меньше 10–15 секунд видимости после завершения.
6. Визуальные подсказки:
   - название станции;
   - цель;
   - как читать result code.
7. Баланс:
   - одно упражнение 30–90 секунд;
   - быстрый reset;
   - отсутствие долгих переходов.

## 13. Что не входит в MVP

Не включать в первую версию:

- автоматическую отправку результатов в backend;
- companion app;
- demo parsing;
- полноценные боты с продвинутым AI;
- сложные сценарии клатчей;
- anti-cheat/anti-fake result validation.

Это можно добавить позже.

## 14. MVP Roadmap

### Этап 1. Приложение и БД

- SQL для `workshop_maps`, `workshop_tasks`, `workshop_results`;
- parser result code;
- экран ввода результата;
- сохранение результата;
- отображение в прогрессе.

### Этап 2. Prototype map

- Hub;
- AIM100;
- SPRAY30;
- STRAFE50;
- result code wall.

### Этап 3. Product test

- 5–10 игроков;
- проверить, удобно ли вводить код;
- понять, какие упражнения реально хочется повторять.

### Этап 4. v1

- FLICK30;
- PREFIRE20;
- MOVE60;
- better scoring;
- графики прогресса по станциям.

## 15. Рекомендация

Начинать нужно не с идеальной карты, а с связки:

```text
CyberGym exercise → workshop station → result code → Android result input → progress trend
```

Это даст уникальную продуктовую петлю:

```text
приложение назначает тренировку
игрок тренируется в CS2
приложение измеряет прогресс
следующий план становится умнее
```

Так CyberGym становится не просто трекером, а настоящей тренировочной системой.
