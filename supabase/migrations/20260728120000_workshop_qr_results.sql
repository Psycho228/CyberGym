begin;

alter table public.practice_sessions
  add column if not exists result jsonb not null default '{}'::jsonb;

create table if not exists public.workshop_result_imports (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  run_id text not null,
  session_kind text not null check (session_kind in ('training', 'practice')),
  session_id uuid not null,
  created_at timestamptz not null default now(),
  unique (user_id, run_id)
);

alter table public.workshop_result_imports enable row level security;

drop policy if exists workshop_result_imports_select_own on public.workshop_result_imports;
create policy workshop_result_imports_select_own
on public.workshop_result_imports
for select
to authenticated
using (user_id = auth.uid());

grant select on public.workshop_result_imports to authenticated;

create or replace function public.start_or_resume_training_v2(p_plan_id uuid)
returns table (
  session_id uuid,
  plan_title text,
  item_id uuid,
  exercise_id uuid,
  exercise_slug text,
  item_position integer,
  exercise_title text,
  exercise_description text,
  instructions text,
  result_type public.exercise_result_type,
  estimated_minutes integer,
  base_xp integer
)
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_user_id uuid := auth.uid();
  v_session_id uuid;
begin
  if v_user_id is null then
    raise exception 'authentication required' using errcode = '28000';
  end if;
  if not exists (
    select 1
    from public.daily_plans as p
    where p.id = p_plan_id and p.user_id = v_user_id
  ) then
    raise exception 'plan not found' using errcode = 'P0002';
  end if;

  select s.id
  into v_session_id
  from public.training_sessions as s
  where s.daily_plan_id = p_plan_id
    and s.user_id = v_user_id
    and s.status = 'in_progress'
  order by s.started_at desc
  limit 1;

  if v_session_id is null then
    insert into public.training_sessions (user_id, daily_plan_id)
    values (v_user_id, p_plan_id)
    returning id into v_session_id;

    update public.daily_plans
    set status = 'started'
    where id = p_plan_id and status = 'assigned';
  end if;

  return query
  select
    v_session_id,
    p.title,
    i.id,
    e.id,
    e.slug,
    i.position,
    e.title,
    e.description,
    e.instructions,
    e.result_type,
    e.estimated_minutes,
    e.base_xp
  from public.daily_plans as p
  join public.daily_plan_items as i on i.daily_plan_id = p.id
  join public.exercises as e on e.id = i.exercise_id
  where p.id = p_plan_id and p.user_id = v_user_id
  order by i.position;
end;
$$;

create or replace function public.start_or_resume_practice_exercise_v2(p_exercise_id uuid)
returns table (
  session_id uuid,
  plan_title text,
  item_id uuid,
  exercise_id uuid,
  exercise_slug text,
  item_position integer,
  exercise_title text,
  exercise_description text,
  instructions text,
  result_type public.exercise_result_type,
  estimated_minutes integer,
  base_xp integer
)
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_user_id uuid := auth.uid();
  v_session_id uuid;
begin
  if v_user_id is null then
    raise exception 'authentication required' using errcode = '28000';
  end if;
  if not exists (
    select 1
    from public.exercises as e
    where e.id = p_exercise_id and e.is_active
  ) then
    raise exception 'exercise not found' using errcode = 'P0002';
  end if;

  select s.id
  into v_session_id
  from public.practice_sessions as s
  where s.user_id = v_user_id
    and s.exercise_id = p_exercise_id
    and s.status = 'in_progress'
  order by s.started_at desc
  limit 1;

  if v_session_id is null then
    insert into public.practice_sessions (user_id, exercise_id)
    values (v_user_id, p_exercise_id)
    returning id into v_session_id;
  end if;

  return query
  select
    v_session_id,
    'Одиночная тренировка'::text,
    e.id,
    e.id,
    e.slug,
    0,
    e.title,
    e.description,
    e.instructions,
    e.result_type,
    e.estimated_minutes,
    e.base_xp
  from public.exercises as e
  where e.id = p_exercise_id and e.is_active;
end;
$$;

create or replace function public.complete_training_session_v2(
  p_session_id uuid,
  p_idempotency_key uuid,
  p_client_completed_at timestamptz,
  p_results jsonb
)
returns table (awarded_xp integer, total_xp bigint, level integer, streak integer)
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_user_id uuid := auth.uid();
  v_run_id text;
begin
  if v_user_id is null then
    raise exception 'authentication required' using errcode = '28000';
  end if;
  if jsonb_typeof(p_results) <> 'array' then
    raise exception 'results must be an array';
  end if;
  if jsonb_array_length(p_results) < 1 or jsonb_array_length(p_results) > 20 then
    raise exception 'invalid result count';
  end if;
  if pg_column_size(p_results) > 65536 then
    raise exception 'results payload is too large';
  end if;
  if exists (
    select 1
    from jsonb_array_elements(p_results) as entry
    where jsonb_typeof(entry->'result') <> 'object'
      or entry->'result'->>'source' <> 'cybergym_workshop'
      or coalesce(length(entry->'result'->>'exercise'), 0) not between 1 and 80
      or coalesce(length(entry->'result'->>'map_name'), 0) not between 1 and 120
      or coalesce(length(entry->'result'->>'run_id'), 0) not between 1 and 120
      or jsonb_typeof(entry->'result'->'metrics') <> 'object'
      or entry->'result'->'metrics' = '{}'::jsonb
      or not exists (
        select 1
        from public.training_sessions as session
        join public.daily_plan_items as item on item.daily_plan_id = session.daily_plan_id
        join public.exercises as exercise on exercise.id = item.exercise_id
        where session.id = p_session_id
          and session.user_id = v_user_id
          and item.id = (entry->>'item_id')::uuid
          and exercise.slug = entry->'result'->>'exercise'
      )
  ) then
    raise exception 'invalid workshop result';
  end if;

  select entry->'result'->>'run_id'
  into v_run_id
  from jsonb_array_elements(p_results) as entry
  limit 1;

  if exists (
    select 1
    from jsonb_array_elements(p_results) as entry
    where entry->'result'->>'run_id' <> v_run_id
  ) then
    raise exception 'results belong to different workshop runs';
  end if;

  insert into public.workshop_result_imports (
    user_id,
    run_id,
    session_kind,
    session_id
  )
  values (
    v_user_id,
    v_run_id,
    'training',
    p_session_id
  )
  on conflict (user_id, run_id) do nothing;

  if not found and not exists (
    select 1
    from public.workshop_result_imports as imported
    where imported.user_id = v_user_id
      and imported.run_id = v_run_id
      and imported.session_kind = 'training'
      and imported.session_id = p_session_id
  ) then
    raise exception 'workshop result has already been used';
  end if;

  return query
  select completion.awarded_xp, completion.total_xp, completion.level, completion.streak
  from public.complete_training_session(
    p_session_id,
    p_idempotency_key,
    p_client_completed_at,
    p_results
  ) as completion;
end;
$$;

create or replace function public.complete_practice_session_v2(
  p_session_id uuid,
  p_idempotency_key uuid,
  p_client_completed_at timestamptz,
  p_result jsonb
)
returns table (awarded_xp integer, total_xp bigint, level integer, streak integer)
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_user_id uuid := auth.uid();
begin
  if v_user_id is null then
    raise exception 'authentication required' using errcode = '28000';
  end if;
  if jsonb_typeof(p_result) <> 'object'
    or p_result->>'source' <> 'cybergym_workshop'
    or coalesce(length(p_result->>'exercise'), 0) not between 1 and 80
    or coalesce(length(p_result->>'map_name'), 0) not between 1 and 120
    or coalesce(length(p_result->>'run_id'), 0) not between 1 and 120
    or jsonb_typeof(p_result->'metrics') <> 'object'
    or p_result->'metrics' = '{}'::jsonb
    or pg_column_size(p_result) > 65536
  then
    raise exception 'invalid workshop result';
  end if;

  if not exists (
    select 1
    from public.practice_sessions as session
    join public.exercises as exercise on exercise.id = session.exercise_id
    where session.id = p_session_id
      and session.user_id = v_user_id
      and exercise.slug = p_result->>'exercise'
  ) then
    raise exception 'practice result does not match session';
  end if;

  insert into public.workshop_result_imports (
    user_id,
    run_id,
    session_kind,
    session_id
  )
  values (
    v_user_id,
    p_result->>'run_id',
    'practice',
    p_session_id
  )
  on conflict (user_id, run_id) do nothing;

  if not found and not exists (
    select 1
    from public.workshop_result_imports as imported
    where imported.user_id = v_user_id
      and imported.run_id = p_result->>'run_id'
      and imported.session_kind = 'practice'
      and imported.session_id = p_session_id
  ) then
    raise exception 'workshop result has already been used';
  end if;

  update public.practice_sessions as session
  set result = p_result
  where session.id = p_session_id
    and session.user_id = v_user_id;
  if not found then
    raise exception 'practice session not found' using errcode = 'P0002';
  end if;

  return query
  select completion.awarded_xp, completion.total_xp, completion.level, completion.streak
  from public.complete_practice_session(
    p_session_id,
    p_idempotency_key,
    p_client_completed_at
  ) as completion;
end;
$$;

revoke all on function public.start_or_resume_training_v2(uuid) from public;
grant execute on function public.start_or_resume_training_v2(uuid) to authenticated;
revoke all on function public.start_or_resume_practice_exercise_v2(uuid) from public;
grant execute on function public.start_or_resume_practice_exercise_v2(uuid) to authenticated;
revoke all on function public.complete_training_session_v2(uuid, uuid, timestamptz, jsonb) from public;
grant execute on function public.complete_training_session_v2(uuid, uuid, timestamptz, jsonb) to authenticated;
revoke all on function public.complete_practice_session_v2(uuid, uuid, timestamptz, jsonb) from public;
grant execute on function public.complete_practice_session_v2(uuid, uuid, timestamptz, jsonb) to authenticated;

notify pgrst, 'reload schema';

commit;
