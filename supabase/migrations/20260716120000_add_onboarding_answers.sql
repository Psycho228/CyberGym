-- Store extended CyberGym onboarding MVP answers without breaking existing profile fields.

begin;

alter table public.profiles
add column if not exists onboarding_answers jsonb not null default '{}'::jsonb;

commit;
