begin;

create or replace function public.complete_training_session(
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
  v_session public.training_sessions%rowtype;
  v_required integer;
  v_submitted integer;
  v_xp integer;
  v_today date;
  v_profile public.profiles%rowtype;
begin
  if v_user_id is null then raise exception 'authentication required' using errcode = '28000'; end if;
  if p_idempotency_key is null then raise exception 'idempotency key required'; end if;
  if jsonb_typeof(p_results) <> 'array' then raise exception 'results must be an array'; end if;

  select * into v_session from public.training_sessions
  where id = p_session_id and user_id = v_user_id for update;
  if not found then raise exception 'session not found' using errcode = 'P0002'; end if;

  if v_session.status = 'completed' then
    return query select v_session.awarded_xp, p.total_xp, p.level, p.current_streak
    from public.profiles p where p.id = v_user_id;
    return;
  end if;

  select count(*) into v_required from public.daily_plan_items
  where daily_plan_id = v_session.daily_plan_id and is_required;
  select count(distinct (r->>'item_id')::uuid) into v_submitted
  from jsonb_array_elements(p_results) r
  join public.daily_plan_items i on i.id = (r->>'item_id')::uuid
  where i.daily_plan_id = v_session.daily_plan_id and i.is_required;
  if v_submitted < v_required then raise exception 'required exercises are incomplete'; end if;

  insert into public.exercise_results (session_id, daily_plan_item_id, result, completed)
  select p_session_id, (r->>'item_id')::uuid, coalesce(r->'result', '{}'::jsonb), true
  from jsonb_array_elements(p_results) r
  join public.daily_plan_items i on i.id = (r->>'item_id')::uuid
  where i.daily_plan_id = v_session.daily_plan_id
  on conflict (session_id, daily_plan_item_id) do update
    set result = excluded.result, completed = true;

  select coalesce(sum(e.base_xp), 0)::integer into v_xp
  from public.daily_plan_items i join public.exercises e on e.id = i.exercise_id
  where i.daily_plan_id = v_session.daily_plan_id and i.is_required;

  insert into public.xp_transactions (user_id, amount, source_type, source_id, metadata)
  values (v_user_id, v_xp, 'training_session', p_session_id,
          jsonb_build_object('idempotency_key', p_idempotency_key))
  on conflict (user_id, source_type, source_id) do nothing;
  if not found then v_xp := 0; end if;

  select * into v_profile from public.profiles where id = v_user_id for update;
  begin
    v_today := (now() at time zone v_profile.timezone)::date;
  exception when invalid_parameter_value then
    v_today := (now() at time zone 'UTC')::date;
  end;

  update public.profiles set
    total_xp = total_xp + v_xp,
    level = floor(sqrt((total_xp + v_xp)::numeric / 100))::integer + 1,
    current_streak = case
      when last_training_local_date = v_today then current_streak
      when last_training_local_date = v_today - 1 then current_streak + 1
      else 1 end,
    longest_streak = greatest(longest_streak, case
      when last_training_local_date = v_today then current_streak
      when last_training_local_date = v_today - 1 then current_streak + 1
      else 1 end),
    last_training_local_date = v_today
  where id = v_user_id;

  update public.training_sessions set status = 'completed', completed_at = now(),
    client_completed_at = p_client_completed_at, idempotency_key = p_idempotency_key,
    awarded_xp = v_xp where id = p_session_id;
  update public.daily_plans set status = 'completed' where id = v_session.daily_plan_id;

  insert into public.user_achievements (user_id, achievement_id)
  select v_user_id, a.id from public.achievements a
  where a.slug = 'first_training' and a.is_active
  on conflict (user_id, achievement_id) do nothing;

  return query select v_xp, p.total_xp, p.level, p.current_streak
  from public.profiles p where p.id = v_user_id;
end;
$$;

revoke all on function public.complete_training_session(uuid, uuid, timestamptz, jsonb) from public;
grant execute on function public.complete_training_session(uuid, uuid, timestamptz, jsonb) to authenticated;

commit;
