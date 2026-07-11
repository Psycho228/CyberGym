begin;

insert into public.games (slug, name) values ('cs2', 'Counter-Strike 2')
on conflict (slug) do update set name = excluded.name, is_active = true;

insert into public.exercise_categories (slug, name, sort_order) values
  ('warmup', 'Разминка', 10), ('aim', 'Aim', 20), ('reaction', 'Реакция', 30),
  ('spray', 'Spray Control', 40), ('movement', 'Movement', 50),
  ('utility', 'Гранаты', 60), ('game_sense', 'Game Sense', 70),
  ('mental', 'Ментальная подготовка', 80), ('review', 'Разбор игры', 90)
on conflict (slug) do update set name = excluded.name, sort_order = excluded.sort_order;

with game as (select id from public.games where slug = 'cs2'),
category as (select id, slug from public.exercise_categories)
insert into public.exercises
  (game_id, category_id, slug, title, description, instructions, result_type,
   estimated_minutes, base_xp, config)
select game.id, category.id, seed.slug, seed.title, seed.description,
       seed.instructions, seed.result_type::public.exercise_result_type,
       seed.minutes, seed.xp, seed.config
from game
cross join lateral (values
  ('warmup', 'warmup_flicks', 'Быстрые флики', 'Короткая разминка перед матчем.',
   'Выполни серии фликов по появляющимся целям.', 'timer', 5, 30, '{"seconds":300}'::jsonb),
  ('aim', 'aim_headshots', 'Только хедшоты', 'Тренировка точности первого выстрела.',
   'Сделай 50 точных попаданий в голову.', 'repetitions', 8, 50, '{"target":50}'::jsonb),
  ('spray', 'ak_spray', 'Контроль AK-47', 'Закрепление spray pattern.',
   'Выполни 5 полных зажимов на средней дистанции.', 'repetitions', 6, 40, '{"target":5}'::jsonb),
  ('movement', 'counter_strafe', 'Counter-strafe', 'Остановка перед точным выстрелом.',
   'Выполни серии A-D с одиночным выстрелом после остановки.', 'timer', 5, 35, '{"seconds":300}'::jsonb),
  ('game_sense', 'demo_review', 'Мини-разбор игры', 'Один осознанный вывод после матча.',
   'Запиши ключевую ошибку и способ её исправить.', 'checklist', 5, 25, '{"items":["Найти ошибку","Сформулировать решение"]}'::jsonb)
) as seed(seed_category, slug, title, description, instructions, result_type, minutes, xp, config)
join category on category.slug = seed.seed_category
on conflict (slug) do update set
  title = excluded.title, description = excluded.description,
  instructions = excluded.instructions, estimated_minutes = excluded.estimated_minutes,
  base_xp = excluded.base_xp, config = excluded.config, is_active = true;

insert into public.training_plan_templates
  (game_id, slug, title, description, goal, estimated_minutes)
select id, 'cs2_daily_foundation', 'Ежедневная база',
       'Короткая программа для стабильного прогресса.', null, 18
from public.games where slug = 'cs2'
on conflict (slug) do update set title = excluded.title, description = excluded.description,
  estimated_minutes = excluded.estimated_minutes, is_active = true;

with template as (select id from public.training_plan_templates where slug = 'cs2_daily_foundation'),
items as (
  select id, row_number() over (order by array_position(
    array['warmup_flicks','aim_headshots','counter_strafe'], slug)) - 1 as position
  from public.exercises where slug in ('warmup_flicks','aim_headshots','counter_strafe')
)
insert into public.training_plan_template_items (template_id, exercise_id, position)
select template.id, items.id, items.position from template cross join items
on conflict (template_id, position) do update set exercise_id = excluded.exercise_id;

insert into public.achievements (slug, title, description, xp_reward, criteria) values
  ('first_training', 'Первый шаг', 'Заверши первую тренировку.', 50, '{"training_count":1}'),
  ('three_day_streak', 'На серии', 'Поддерживай серию 3 дня.', 100, '{"streak":3}'),
  ('ten_trainings', 'Вошёл в ритм', 'Заверши 10 тренировок.', 150, '{"training_count":10}')
on conflict (slug) do update set title = excluded.title, description = excluded.description,
  xp_reward = excluded.xp_reward, criteria = excluded.criteria, is_active = true;

commit;
