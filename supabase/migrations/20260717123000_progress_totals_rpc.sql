-- Reliable progress totals for the Progress screen.
-- Keeps UI recency limits separate from aggregate counters.

create or replace function public.get_progress_totals()
returns table (
  total_trainings integer,
  completed_training_sessions integer,
  completed_practice_sessions integer,
  total_awarded_xp bigint
)
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

  return query
  with training as (
    select
      count(*)::integer as completed_count,
      coalesce(sum(s.awarded_xp), 0)::bigint as awarded_xp
    from public.training_sessions as s
    where s.user_id = v_user_id
      and s.status = 'completed'
  ),
  practice as (
    select
      count(*)::integer as completed_count,
      coalesce(sum(s.awarded_xp), 0)::bigint as awarded_xp
    from public.practice_sessions as s
    where s.user_id = v_user_id
      and s.status = 'completed'
  )
  select
    (training.completed_count + practice.completed_count)::integer as total_trainings,
    training.completed_count as completed_training_sessions,
    practice.completed_count as completed_practice_sessions,
    (training.awarded_xp + practice.awarded_xp)::bigint as total_awarded_xp
  from training, practice;
end;
$$;

revoke all on function public.get_progress_totals() from public;
grant execute on function public.get_progress_totals() to authenticated;
