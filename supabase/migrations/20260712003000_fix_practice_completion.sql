begin;

create or replace function public.complete_practice_session(
  p_session_id uuid,
  p_idempotency_key uuid,
  p_client_completed_at timestamptz
)
returns table (awarded_xp integer, total_xp bigint, level integer, streak integer)
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_user_id uuid := auth.uid();
  v_session public.practice_sessions%rowtype;
  v_xp integer;
  v_today date;
  v_profile public.profiles%rowtype;
begin
  if v_user_id is null then raise exception 'authentication required' using errcode = '28000'; end if;
  if p_idempotency_key is null then raise exception 'idempotency key required'; end if;

  select * into v_session from public.practice_sessions
  where id = p_session_id and user_id = v_user_id for update;
  if not found then raise exception 'practice session not found' using errcode = 'P0002'; end if;

  if v_session.status = 'completed' then
    return query select v_session.awarded_xp, p.total_xp, p.level, p.current_streak
    from public.profiles p where p.id = v_user_id;
    return;
  end if;

  select e.base_xp into v_xp
  from public.exercises e
  where e.id = v_session.exercise_id and e.is_active;
  v_xp := coalesce(v_xp, 0);

  if v_xp > 0 then
    insert into public.xp_transactions (user_id, amount, source_type, source_id, metadata)
    values (v_user_id, v_xp, 'practice_session', p_session_id,
            jsonb_build_object('idempotency_key', p_idempotency_key))
    on conflict (user_id, source_type, source_id) do nothing;
    if not found then v_xp := 0; end if;
  end if;

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

  update public.practice_sessions set
    status = 'completed',
    completed_at = now(),
    client_completed_at = p_client_completed_at,
    idempotency_key = p_idempotency_key,
    awarded_xp = v_xp
  where id = p_session_id;

  insert into public.user_achievements (user_id, achievement_id)
  select v_user_id, a.id from public.achievements a
  where a.slug = 'first_training' and a.is_active
  on conflict (user_id, achievement_id) do nothing;

  return query select v_xp, p.total_xp, p.level, p.current_streak
  from public.profiles p where p.id = v_user_id;
end;
$$;

revoke all on function public.complete_practice_session(uuid, uuid, timestamptz) from public;
grant execute on function public.complete_practice_session(uuid, uuid, timestamptz) to authenticated;

commit;
