-- Verify that CyberGym onboarding MVP answers are stored for completed profiles.
-- Run in Supabase SQL Editor after completing onboarding in the app.

with required_keys(key) as (
  values
    ('nickname'),
    ('goal'),
    ('premier_rating'),
    ('faceit_level'),
    ('modes'),
    ('favorite_maps'),
    ('training_duration_minutes'),
    ('training_frequency_days'),
    ('weak_spots'),
    ('tools'),
    ('connect_faceit'),
    ('self_scores')
),
completed_profiles as (
  select
    id,
    nickname,
    current_rank,
    primary_goal,
    primary_goals,
    daily_minutes,
    onboarding_answers
  from public.profiles
  where onboarding_completed = true
)
select
  p.id,
  p.nickname,
  p.current_rank,
  p.primary_goal,
  p.primary_goals,
  p.daily_minutes,
  coalesce(
    array_agg(k.key order by k.key) filter (
      where not p.onboarding_answers ? k.key
    ),
    '{}'::text[]
  ) as missing_answer_keys,
  case
    when (p.onboarding_answers ->> 'connect_faceit')::boolean is true
      and not p.onboarding_answers ? 'faceit_player'
    then 'faceit_player'
    else null
  end as missing_connected_faceit_key,
  p.onboarding_answers
from completed_profiles p
cross join required_keys k
group by
  p.id,
  p.nickname,
  p.current_rank,
  p.primary_goal,
  p.primary_goals,
  p.daily_minutes,
  p.onboarding_answers
order by p.id;
