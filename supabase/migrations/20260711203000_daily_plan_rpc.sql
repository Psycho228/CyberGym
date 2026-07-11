begin;

create or replace function public.get_or_create_daily_plan(target_date date default current_date)
returns table (
  plan_id uuid,
  plan_date date,
  plan_title text,
  plan_status text,
  estimated_minutes integer,
  exercise_count bigint
)
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_user_id uuid := auth.uid();
  v_plan public.daily_plans%rowtype;
  v_template public.training_plan_templates%rowtype;
begin
  if v_user_id is null then raise exception 'authentication required' using errcode = '28000'; end if;

  select * into v_plan from public.daily_plans
  where user_id = v_user_id and plan_date = target_date;

  if not found then
    select t.* into v_template
    from public.training_plan_templates t
    join public.profiles p on p.id = v_user_id
    where t.is_active and (t.goal is null or t.goal = p.primary_goal)
    order by case when t.goal = p.primary_goal then 0 else 1 end, t.version desc
    limit 1;

    if v_template.id is null then raise exception 'no active training template'; end if;

    insert into public.daily_plans (user_id, template_id, plan_date, title, estimated_minutes)
    values (v_user_id, v_template.id, target_date, v_template.title, v_template.estimated_minutes)
    on conflict (user_id, plan_date) do update set user_id = excluded.user_id
    returning * into v_plan;

    insert into public.daily_plan_items
      (daily_plan_id, exercise_id, position, is_required, config_snapshot)
    select v_plan.id, i.exercise_id, i.position, i.is_required,
           e.config || i.overrides
    from public.training_plan_template_items i
    join public.exercises e on e.id = i.exercise_id and e.is_active
    where i.template_id = v_template.id
    on conflict (daily_plan_id, position) do nothing;
  end if;

  return query
  select v_plan.id, v_plan.plan_date, v_plan.title, v_plan.status,
         v_plan.estimated_minutes, count(i.id)
  from public.daily_plan_items i where i.daily_plan_id = v_plan.id
  group by v_plan.id, v_plan.plan_date, v_plan.title, v_plan.status, v_plan.estimated_minutes;
end;
$$;

revoke all on function public.get_or_create_daily_plan(date) from public;
grant execute on function public.get_or_create_daily_plan(date) to authenticated;

commit;
