begin;

create or replace function public.start_or_resume_training(p_plan_id uuid)
returns table (
  session_id uuid,
  plan_title text,
  item_id uuid,
  exercise_id uuid,
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
  if v_user_id is null then raise exception 'authentication required' using errcode = '28000'; end if;
  if not exists (select 1 from public.daily_plans p where p.id = p_plan_id and p.user_id = v_user_id)
  then raise exception 'plan not found' using errcode = 'P0002'; end if;

  select s.id into v_session_id from public.training_sessions s
  where s.daily_plan_id = p_plan_id and s.user_id = v_user_id and s.status = 'in_progress'
  order by s.started_at desc limit 1;

  if v_session_id is null then
    insert into public.training_sessions (user_id, daily_plan_id)
    values (v_user_id, p_plan_id) returning id into v_session_id;
    update public.daily_plans set status = 'started'
    where id = p_plan_id and status = 'assigned';
  end if;

  return query
  select v_session_id, p.title, i.id, e.id, i.position, e.title, e.description,
         e.instructions, e.result_type, e.estimated_minutes, e.base_xp
  from public.daily_plans p
  join public.daily_plan_items i on i.daily_plan_id = p.id
  join public.exercises e on e.id = i.exercise_id
  where p.id = p_plan_id and p.user_id = v_user_id
  order by i.position;
end;
$$;

revoke all on function public.start_or_resume_training(uuid) from public;
grant execute on function public.start_or_resume_training(uuid) to authenticated;

commit;
