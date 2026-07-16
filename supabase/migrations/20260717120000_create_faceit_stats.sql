-- Store the latest FACEIT statistics snapshot for each CyberGym profile.
-- Requires 20260711195000_create_profiles.sql.

create table if not exists public.faceit_stats (
  user_id uuid primary key references public.profiles(id) on delete cascade,
  player_id text not null,
  nickname text,
  skill_level integer,
  faceit_elo integer,
  game_player_id text,
  matches integer,
  win_rate text,
  average_kd text,
  headshots text,
  raw jsonb not null default '{}'::jsonb,
  synced_at timestamptz not null default now(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint faceit_stats_player_id_not_blank check (length(trim(player_id)) > 0)
);

create index if not exists faceit_stats_player_id_idx
  on public.faceit_stats(player_id);

create or replace function public.set_faceit_stats_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists faceit_stats_set_updated_at on public.faceit_stats;
create trigger faceit_stats_set_updated_at
before update on public.faceit_stats
for each row
execute function public.set_faceit_stats_updated_at();

alter table public.faceit_stats enable row level security;

drop policy if exists faceit_stats_select_own on public.faceit_stats;
create policy faceit_stats_select_own
on public.faceit_stats
for select
to authenticated
using (user_id = auth.uid());

drop policy if exists faceit_stats_insert_own on public.faceit_stats;
create policy faceit_stats_insert_own
on public.faceit_stats
for insert
to authenticated
with check (user_id = auth.uid());

drop policy if exists faceit_stats_update_own on public.faceit_stats;
create policy faceit_stats_update_own
on public.faceit_stats
for update
to authenticated
using (user_id = auth.uid())
with check (user_id = auth.uid());

grant select, insert, update on public.faceit_stats to authenticated;
