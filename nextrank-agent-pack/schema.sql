create extension if not exists pgcrypto;

create type player_goal as enum (
  'rank_up',
  'aim',
  'movement',
  'spray',
  'discipline',
  'teamplay'
);

create type exercise_result_type as enum (
  'timer',
  'repetitions',
  'score',
  'checklist',
  'external_task',
  'self_rating'
);

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  nickname text not null default '',
  timezone text not null default 'UTC',
  current_rank text,
  primary_goal player_goal,
  daily_minutes integer not null default 15 check (daily_minutes between 5 and 120),
  reminder_time time,
  onboarding_completed boolean not null default false,
  total_xp bigint not null default 0 check (total_xp >= 0),
  level integer not null default 1 check (level >= 1),
  current_streak integer not null default 0 check (current_streak >= 0),
  longest_streak integer not null default 0 check (longest_streak >= 0),
  last_training_local_date date,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.games (
  id uuid primary key default gen_random_uuid(),
  slug text not null unique,
  name text not null,
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

create table public.exercise_categories (
  id uuid primary key default gen_random_uuid(),
  slug text not null unique,
  name text not null,
  sort_order integer not null default 0
);

create table public.exercises (
  id uuid primary key default gen_random_uuid(),
  game_id uuid not null references public.games(id),
  category_id uuid not null references public.exercise_categories(id),
  slug text not null unique,
  title text not null,
  description text not null,
  instructions text not null,
  result_type exercise_result_type not null,
  estimated_minutes integer not null check (estimated_minutes between 1 and 120),
  base_xp integer not null default 0 check (base_xp >= 0),
  media_path text,
  external_uri text,
  config jsonb not null default '{}'::jsonb,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.training_plan_templates (
  id uuid primary key default gen_random_uuid(),
  game_id uuid not null references public.games(id),
  slug text not null unique,
  title text not null,
  description text,
  min_rank text,
  max_rank text,
  goal player_goal,
  estimated_minutes integer not null,
  is_active boolean not null default true,
  version integer not null default 1,
  created_at timestamptz not null default now()
);

create table public.training_plan_template_items (
  id uuid primary key default gen_random_uuid(),
  template_id uuid not null references public.training_plan_templates(id) on delete cascade,
  exercise_id uuid not null references public.exercises(id),
  position integer not null,
  is_required boolean not null default true,
  overrides jsonb not null default '{}'::jsonb,
  unique(template_id, position)
);

create table public.daily_plans (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  template_id uuid references public.training_plan_templates(id),
  plan_date date not null,
  title text not null,
  estimated_minutes integer not null,
  status text not null default 'assigned' check (status in ('assigned', 'started', 'completed', 'expired')),
  created_at timestamptz not null default now(),
  unique(user_id, plan_date)
);

create table public.daily_plan_items (
  id uuid primary key default gen_random_uuid(),
  daily_plan_id uuid not null references public.daily_plans(id) on delete cascade,
  exercise_id uuid not null references public.exercises(id),
  position integer not null,
  is_required boolean not null default true,
  config_snapshot jsonb not null default '{}'::jsonb,
  unique(daily_plan_id, position)
);

create table public.training_sessions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  daily_plan_id uuid not null references public.daily_plans(id) on delete cascade,
  status text not null default 'in_progress' check (status in ('in_progress', 'completed', 'abandoned')),
  started_at timestamptz not null default now(),
  completed_at timestamptz,
  client_completed_at timestamptz,
  idempotency_key uuid,
  awarded_xp integer not null default 0 check (awarded_xp >= 0),
  created_at timestamptz not null default now()
);

create unique index training_sessions_one_completed_per_plan
on public.training_sessions(daily_plan_id)
where status = 'completed';

create unique index training_sessions_idempotency
on public.training_sessions(user_id, idempotency_key)
where idempotency_key is not null;

create table public.exercise_results (
  id uuid primary key default gen_random_uuid(),
  session_id uuid not null references public.training_sessions(id) on delete cascade,
  daily_plan_item_id uuid not null references public.daily_plan_items(id),
  result jsonb not null,
  completed boolean not null default false,
  created_at timestamptz not null default now(),
  unique(session_id, daily_plan_item_id)
);

create table public.xp_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  amount integer not null check (amount <> 0),
  source_type text not null,
  source_id uuid not null,
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  unique(user_id, source_type, source_id)
);

create table public.achievements (
  id uuid primary key default gen_random_uuid(),
  slug text not null unique,
  title text not null,
  description text not null,
  icon_path text,
  xp_reward integer not null default 0 check (xp_reward >= 0),
  criteria jsonb not null,
  is_active boolean not null default true
);

create table public.user_achievements (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  achievement_id uuid not null references public.achievements(id),
  unlocked_at timestamptz not null default now(),
  unique(user_id, achievement_id)
);

create table public.push_tokens (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  token text not null unique,
  platform text not null default 'android',
  device_id text,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index daily_plans_user_date_idx
  on public.daily_plans(user_id, plan_date desc);

create index training_sessions_user_completed_idx
  on public.training_sessions(user_id, completed_at desc);

create index exercise_results_session_idx
  on public.exercise_results(session_id);

create index xp_transactions_user_created_idx
  on public.xp_transactions(user_id, created_at desc);

create index user_achievements_user_unlocked_idx
  on public.user_achievements(user_id, unlocked_at desc);

create index exercises_lookup_idx
  on public.exercises(game_id, category_id, is_active);

alter table public.profiles enable row level security;
alter table public.daily_plans enable row level security;
alter table public.daily_plan_items enable row level security;
alter table public.training_sessions enable row level security;
alter table public.exercise_results enable row level security;
alter table public.xp_transactions enable row level security;
alter table public.user_achievements enable row level security;
alter table public.push_tokens enable row level security;

create policy "profiles_select_own"
on public.profiles for select
using (id = auth.uid());

create policy "profiles_update_own_safe_fields"
on public.profiles for update
using (id = auth.uid())
with check (id = auth.uid());

create policy "daily_plans_select_own"
on public.daily_plans for select
using (user_id = auth.uid());

create policy "daily_plan_items_select_own"
on public.daily_plan_items for select
using (
  exists (
    select 1
    from public.daily_plans p
    where p.id = daily_plan_id
      and p.user_id = auth.uid()
  )
);

create policy "training_sessions_select_own"
on public.training_sessions for select
using (user_id = auth.uid());

create policy "training_sessions_insert_own"
on public.training_sessions for insert
with check (user_id = auth.uid());

create policy "exercise_results_select_own"
on public.exercise_results for select
using (
  exists (
    select 1
    from public.training_sessions s
    where s.id = session_id
      and s.user_id = auth.uid()
  )
);

create policy "exercise_results_insert_own"
on public.exercise_results for insert
with check (
  exists (
    select 1
    from public.training_sessions s
    where s.id = session_id
      and s.user_id = auth.uid()
      and s.status = 'in_progress'
  )
);

create policy "xp_transactions_select_own"
on public.xp_transactions for select
using (user_id = auth.uid());

create policy "user_achievements_select_own"
on public.user_achievements for select
using (user_id = auth.uid());

create policy "push_tokens_all_own"
on public.push_tokens for all
using (user_id = auth.uid())
with check (user_id = auth.uid());

insert into public.games (slug, name)
values ('cs2', 'Counter-Strike 2')
on conflict (slug) do nothing;

insert into public.exercise_categories (slug, name, sort_order) values
  ('warmup', 'Разминка', 10),
  ('aim', 'Aim', 20),
  ('reaction', 'Реакция', 30),
  ('spray', 'Spray Control', 40),
  ('movement', 'Movement', 50),
  ('utility', 'Гранаты', 60),
  ('game_sense', 'Game Sense', 70),
  ('mental', 'Ментальная подготовка', 80),
  ('review', 'Разбор игры', 90)
on conflict (slug) do nothing;
