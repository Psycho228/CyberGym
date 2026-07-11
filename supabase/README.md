# Supabase migrations

Apply migrations to the CyberGym database in filename order:

1. `20260711195000_create_profiles.sql`
2. `20260711201000_create_training_tables.sql`
3. `20260711202000_seed_cs2_content.sql`
4. `20260711203000_daily_plan_rpc.sql`
5. `20260711204000_start_training_rpc.sql`
6. `20260711205000_complete_training_rpc.sql`
7. `20260712002000_practice_sessions.sql`
8. `20260712003000_fix_practice_completion.sql`
9. `20260712004000_harden_practice_completion.sql`

For the current self-hosted installation, open the CyberGym Studio SQL Editor and
run `migrations/20260711195000_create_profiles.sql`, or copy it to the VPS and run:

```bash
docker exec -i cybergym-supabase-db \
  psql -v ON_ERROR_STOP=1 -U postgres -d postgres \
  < 20260711195000_create_profiles.sql
```

Apply every file separately with `ON_ERROR_STOP=1`. Do not run CyberGym
migrations against the old `supabase-db` container.
