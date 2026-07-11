-- NextRank training, progress, achievements and device tables.
-- Requires 20260711195000_create_profiles.sql.

begin;

do $$ begin
  create type public.exercise_result_type as enum
    ('timer', 'repetitions', 'score', 'checklist', 'external_task', 'self_rating');
exception when duplicate_object then null; end $$;

create table if not exists public.games (
  id uuid primary key default gen_random_uuid(),
  slug text not null unique,
  name text not null,
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists public.exercise_categories (
  id uuid primary key default gen_random_uuid(),
  slug text not null unique,
  name text not null,
  sort_order integer not null default 0
);

create table if not exists public.exercises (
  id uuid primary key default gen_random_uuid(),
  game_id uuid not null references public.games(id),
  category_id uuid not null references public.exercise_categories(id),
  slug text not null unique,
  title text not null,
  description text not null,
  instructions text not null,
  result_type public.exercise_result_type not null,
  estimated_minutes integer not null check (estimated_minutes between 1 and 120),
  base_xp integer not null default 0 check (base_xp >= 0),
  media_path text,
  external_uri text,
  config jsonb not null default '{}'::jsonb,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.training_plan_templates (
  id uuid primary key default gen_random_uuid(),
  game_id uuid not null references public.games(id),
  slug text not null unique,
  title text not null,
  description text,
  min_rank text,
  max_rank text,
  goal public.player_goal,
  estimated_minutes integer not null check (estimated_minutes between 1 and 120),
  is_active boolean not null default true,
  version integer not null default 1 check (version > 0),
  created_at timestamptz not null default now()
);

create table if not exists public.training_plan_template_items (
  id uuid primary key default gen_random_uuid(),
  template_id uuid not null references public.training_plan_templates(id) on delete cascade,
  exercise_id uuid not null references public.exercises(id),
  position integer not null check (position >= 0),
  is_required boolean not null default true,
  overrides jsonb not null default '{}'::jsonb,
  unique (template_id, position)
);

create table if not exists public.daily_plans (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  template_id uuid references public.training_plan_templates(id),
  plan_date date not null,
  title text not null,
  estimated_minutes integer not null check (estimated_minutes > 0),
  status text not null default 'assigned'
    check (status in ('assigned', 'started', 'completed', 'expired')),
  created_at timestamptz not null default now(),
  unique (user_id, plan_date)
);

create table if not exists public.daily_plan_items (
  id uuid primary key default gen_random_uuid(),
  daily_plan_id uuid not null references public.daily_plans(id) on delete cascade,
  exercise_id uuid not null references public.exercises(id),
  position integer not null check (position >= 0),
  is_required boolean not null default true,
  config_snapshot jsonb not null default '{}'::jsonb,
  unique (daily_plan_id, position)
);

create table if not exists public.training_sessions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  daily_plan_id uuid not null references public.daily_plans(id) on delete cascade,
  status text not null default 'in_progress'
    check (status in ('in_progress', 'completed', 'abandoned')),
  started_at timestamptz not null default now(),
  completed_at timestamptz,
  client_completed_at timestamptz,
  idempotency_key uuid,
  awarded_xp integer not null default 0 check (awarded_xp >= 0),
  created_at timestamptz not null default now()
);

create table if not exists public.exercise_results (
  id uuid primary key default gen_random_uuid(),
  session_id uuid not null references public.training_sessions(id) on delete cascade,
  daily_plan_item_id uuid not null references public.daily_plan_items(id),
  result jsonb not null,
  completed boolean not null default false,
  created_at timestamptz not null default now(),
  unique (session_id, daily_plan_item_id)
);

create table if not exists public.xp_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  amount integer not null check (amount <> 0),
  source_type text not null,
  source_id uuid not null,
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  unique (user_id, source_type, source_id)
);

create table if not exists public.achievements (
  id uuid primary key default gen_random_uuid(),
  slug text not null unique,
  title text not null,
  description text not null,
  icon_path text,
  xp_reward integer not null default 0 check (xp_reward >= 0),
  criteria jsonb not null,
  is_active boolean not null default true
);

create table if not exists public.user_achievements (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  achievement_id uuid not null references public.achievements(id),
  unlocked_at timestamptz not null default now(),
  unique (user_id, achievement_id)
);

create table if not exists public.push_tokens (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  token text not null unique,
  platform text not null default 'android' check (platform = 'android'),
  device_id text,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create unique index if not exists training_sessions_one_completed_per_plan
  on public.training_sessions(daily_plan_id) where status = 'completed';
create unique index if not exists training_sessions_idempotency
  on public.training_sessions(user_id, idempotency_key) where idempotency_key is not null;
create index if not exists daily_plans_user_date_idx on public.daily_plans(user_id, plan_date desc);
create index if not exists training_sessions_user_completed_idx on public.training_sessions(user_id, completed_at desc);
create index if not exists exercise_results_session_idx on public.exercise_results(session_id);
create index if not exists xp_transactions_user_created_idx on public.xp_transactions(user_id, created_at desc);
create index if not exists user_achievements_user_unlocked_idx on public.user_achievements(user_id, unlocked_at desc);
create index if not exists exercises_lookup_idx on public.exercises(game_id, category_id, is_active);

alter table public.games enable row level security;
alter table public.exercise_categories enable row level security;
alter table public.exercises enable row level security;
alter table public.training_plan_templates enable row level security;
alter table public.training_plan_template_items enable row level security;
alter table public.daily_plans enable row level security;
alter table public.daily_plan_items enable row level security;
alter table public.training_sessions enable row level security;
alter table public.exercise_results enable row level security;
alter table public.xp_transactions enable row level security;
alter table public.achievements enable row level security;
alter table public.user_achievements enable row level security;
alter table public.push_tokens enable row level security;

drop policy if exists public_games_read on public.games;
create policy public_games_read on public.games for select to authenticated using (is_active);
drop policy if exists public_categories_read on public.exercise_categories;
create policy public_categories_read on public.exercise_categories for select to authenticated using (true);
drop policy if exists public_exercises_read on public.exercises;
create policy public_exercises_read on public.exercises for select to authenticated using (is_active);
drop policy if exists public_templates_read on public.training_plan_templates;
create policy public_templates_read on public.training_plan_templates for select to authenticated using (is_active);
drop policy if exists public_template_items_read on public.training_plan_template_items;
create policy public_template_items_read on public.training_plan_template_items for select to authenticated
using (exists (select 1 from public.training_plan_templates t where t.id = template_id and t.is_active));
drop policy if exists achievements_read on public.achievements;
create policy achievements_read on public.achievements for select to authenticated using (is_active);

drop policy if exists daily_plans_select_own on public.daily_plans;
create policy daily_plans_select_own on public.daily_plans for select to authenticated using (user_id = auth.uid());
drop policy if exists daily_plan_items_select_own on public.daily_plan_items;
create policy daily_plan_items_select_own on public.daily_plan_items for select to authenticated using
  (exists (select 1 from public.daily_plans p where p.id = daily_plan_id and p.user_id = auth.uid()));
drop policy if exists training_sessions_select_own on public.training_sessions;
create policy training_sessions_select_own on public.training_sessions for select to authenticated using (user_id = auth.uid());
drop policy if exists exercise_results_select_own on public.exercise_results;
create policy exercise_results_select_own on public.exercise_results for select to authenticated using
  (exists (select 1 from public.training_sessions s where s.id = session_id and s.user_id = auth.uid()));
drop policy if exists xp_transactions_select_own on public.xp_transactions;
create policy xp_transactions_select_own on public.xp_transactions for select to authenticated using (user_id = auth.uid());
drop policy if exists user_achievements_select_own on public.user_achievements;
create policy user_achievements_select_own on public.user_achievements for select to authenticated using (user_id = auth.uid());
drop policy if exists push_tokens_all_own on public.push_tokens;
create policy push_tokens_all_own on public.push_tokens for all to authenticated
  using (user_id = auth.uid()) with check (user_id = auth.uid());

grant select on public.games, public.exercise_categories, public.exercises,
  public.training_plan_templates, public.training_plan_template_items,
  public.achievements to authenticated;
grant select on public.daily_plans, public.daily_plan_items, public.training_sessions,
  public.exercise_results, public.xp_transactions, public.user_achievements to authenticated;
grant select, insert, update, delete on public.push_tokens to authenticated;

commit;
