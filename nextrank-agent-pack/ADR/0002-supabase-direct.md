# ADR-0002: Direct Supabase Access for MVP

Status: Accepted

## Context

Есть собственный VPS с self-hosted Supabase и S3-совместимое хранилище. Создание отдельного backend увеличит сроки MVP.

## Decision

Android-приложение обращается к Supabase напрямую через Kotlin SDK.

Привилегированные операции выполняются через:

- PostgreSQL RPC;
- database transaction;
- Edge Functions при необходимости.

Repository-интерфейсы скрывают конкретный транспорт.

## Consequences

Положительные:

- меньше кода;
- быстрее MVP;
- проще эксплуатация;
- использование Auth, PostgreSQL и RLS.

Риски:

- бизнес-логика может расползтись по SQL;
- сложные сценарии потребуют backend;
- ошибки RLS критичны.

Меры:

- SQL оставлять компактным;
- критические операции покрывать integration tests;
- не связывать presentation с Supabase;
- при росте продукта добавить API без полной переработки UI.
