-- CyberGym MVP: profile data used by onboarding.
-- Run as postgres in Supabase Studio SQL Editor or with psql.

begin;

create extension if not exists pgcrypto;

do $$
begin
    create type public.player_goal as enum (
        'rank_up',
        'aim',
        'movement',
        'spray',
        'discipline',
        'teamplay'
    );
exception
    when duplicate_object then null;
end
$$;

create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    nickname text not null default '',
    timezone text not null default 'UTC',
    current_rank text,
    primary_goal public.player_goal,
    daily_minutes integer not null default 15
        check (daily_minutes between 5 and 120),
    reminder_time time,
    onboarding_completed boolean not null default false,
    total_xp bigint not null default 0 check (total_xp >= 0),
    level integer not null default 1 check (level >= 1),
    current_streak integer not null default 0 check (current_streak >= 0),
    longest_streak integer not null default 0 check (longest_streak >= 0),
    last_training_local_date date,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint profiles_nickname_length
        check (char_length(trim(nickname)) between 0 and 40)
);

alter table public.profiles enable row level security;

drop policy if exists profiles_select_own on public.profiles;
create policy profiles_select_own
on public.profiles
for select
to authenticated
using (id = auth.uid());

drop policy if exists profiles_update_own on public.profiles;
create policy profiles_update_own
on public.profiles
for update
to authenticated
using (id = auth.uid())
with check (id = auth.uid());

create or replace function public.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.profiles (id)
    values (new.id)
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_user();

-- Backfill profiles for users registered before this migration.
insert into public.profiles (id)
select id
from auth.users
on conflict (id) do nothing;

grant usage on schema public to authenticated;
grant select, update on public.profiles to authenticated;

commit;

