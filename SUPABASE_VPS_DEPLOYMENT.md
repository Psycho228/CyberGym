# Безопасное развёртывание второго Supabase-проекта на одном VPS

Эта инструкция описывает запуск нового self-hosted Supabase-проекта `cybergym` на VPS, где уже работает другой Supabase-проект. Основной принцип: новый проект должен иметь отдельные Compose-ресурсы, данные, секреты, порты и домен. Команды старого проекта не изменяются до создания проверенной резервной копии.

> Не выполняйте `docker compose down -v`, `docker volume rm`, `docker system prune --volumes` или удаление каталогов данных. Эти команды могут безвозвратно удалить базу данных и Storage.

## 1. Целевая схема

```text
/srv/supabase/
├── old-project/                # существующий проект, не изменяем
│   ├── docker-compose.yml
│   ├── .env
│   └── volumes/
└── cybergym/                   # новый независимый проект
    ├── docker-compose.yml
    ├── .env                    # секреты, не добавлять в Git
    ├── utils/
    └── volumes/
```

Ожидаемая маршрутизация:

```text
https://supabase.old-domain.ru  -> старый Supabase API gateway
https://supabase.cybergym.ru    -> новый Supabase API gateway
```

Оба домена используют общие внешние порты `80/443` reverse proxy. Сам reverse proxy направляет запросы к разным локальным портам или Docker-сетям.

## 2. Что должно быть уникальным

Для нового проекта обязательно создаются отдельные:

- каталог на сервере;
- `COMPOSE_PROJECT_NAME`;
- Docker network и volumes;
- bind-mount каталоги базы данных и Storage;
- Postgres-пароль;
- JWT/API-ключи и остальные секреты;
- пароль Supabase Studio;
- публичный домен;
- опубликованные host-порты;
- резервные копии и политика восстановления.

Docker Compose автоматически добавляет имя проекта к именам контейнеров, сетей и обычных named volumes. Поэтому уникальный `COMPOSE_PROJECT_NAME` является главным уровнем изоляции, но не защищает bind mounts и volumes с явно заданным `name:`.

## 3. Требования к серверу

Для одного полного Supabase-стека официальный ориентир составляет минимум 4 ГБ RAM, 2 CPU и 40 ГБ SSD; рекомендуются 8+ ГБ RAM, 4+ CPU и 80+ ГБ SSD. Для двух стеков нужно учитывать реальную нагрузку, резерв под Postgres и рост Storage.

Проверка ресурсов:

```bash
free -h
df -h
df -i
nproc
docker stats --no-stream
```

Если ресурсов мало, можно не включать необязательные сервисы, например Analytics/Logflare, Vector, Functions, imgproxy или Realtime — только если они действительно не используются приложением.

## 4. Инвентаризация существующего проекта

Сначала определить каталог и Compose project name старого стека:

```bash
docker compose ls
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}\t{{.Status}}'
docker volume ls
docker network ls
```

Посмотреть Compose-метки конкретного контейнера:

```bash
docker inspect OLD_CONTAINER_NAME \
  --format '{{ index .Config.Labels "com.docker.compose.project" }}'
```

В каталоге старого проекта сохранить итоговую конфигурацию:

```bash
cd /srv/supabase/old-project
docker compose config > /root/old-supabase-compose.snapshot.yml
docker compose ps
```

Файл после `docker compose config` может содержать раскрытые секреты. Он должен храниться с правами root и не должен попадать в Git или пересылаться без шифрования:

```bash
chmod 600 /root/old-supabase-compose.snapshot.yml
```

Записать занятые порты:

```bash
sudo ss -lntup
```

Особенно проверить `80`, `443`, `5432`, `6543`, `8000`, `8443` и любые порты, опубликованные старым Supabase.

## 5. Резервная копия старого проекта

Перед запуском нового стека должна существовать проверяемая резервная копия старой базы и Storage. Недостаточно просто скопировать каталог работающего Postgres: такая копия может оказаться несогласованной.

Минимальный логический дамп базы выполняется через `pg_dump` совместимой версии. Точное имя контейнера и пользователя нужно взять из старого Compose:

```bash
docker exec OLD_DB_CONTAINER pg_dump \
  -U postgres \
  -d postgres \
  -Fc \
  -f /tmp/old-project.dump

docker cp OLD_DB_CONTAINER:/tmp/old-project.dump \
  /root/backups/old-project-$(date +%F).dump
```

После копирования:

```bash
ls -lh /root/backups/
pg_restore --list /root/backups/old-project-YYYY-MM-DD.dump | head
```

Также отдельно архивируются:

- каталог файлов Supabase Storage;
- `.env` старого проекта;
- Compose-файлы и overrides;
- конфигурация Nginx/Caddy;
- пользовательские Edge Functions;
- миграции и SQL-функции, если они хранятся отдельно.

Для production-системы лучше сделать тестовое восстановление дампа в отдельную временную БД. Резервная копия считается надёжной только после проверки восстановления.

## 6. Создание отдельного каталога

```bash
sudo mkdir -p /srv/supabase/cybergym
sudo chown -R "$USER":"$USER" /srv/supabase/cybergym
cd /srv/supabase/cybergym
```

В этот каталог нужно поместить свежую официальную Docker-конфигурацию Supabase. Не распаковывайте и не клонируйте её поверх каталога старого проекта.

Перед использованием зафиксируйте версию или commit официальной конфигурации, чтобы последующие обновления были контролируемыми.

## 7. Уникальное имя Compose-проекта

Добавить в новый `.env`:

```dotenv
COMPOSE_PROJECT_NAME=supabase_cybergym
```

Все команды безопаснее выполнять с явным именем:

```bash
docker compose -p supabase_cybergym config --quiet
docker compose -p supabase_cybergym up -d
docker compose -p supabase_cybergym ps
```

Не использовать для нового стека имя, показанное в `docker compose ls` у старого проекта.

## 8. Проверка volumes и bind mounts

Нужно проверить все разделы `volumes:` в новом Compose:

```bash
docker compose -p supabase_cybergym config > /tmp/cybergym-compose.rendered.yml
grep -n -A5 -B5 '/var/lib/postgresql\|/var/lib/storage\|source:' \
  /tmp/cybergym-compose.rendered.yml
```

Безопасный bind mount использует путь внутри нового каталога, например:

```yaml
services:
  db:
    volumes:
      - ./volumes/db/data:/var/lib/postgresql/data

  storage:
    volumes:
      - ./volumes/storage:/var/lib/storage
```

Такой путь безопасен только при запуске Compose из `/srv/supabase/cybergym` либо при корректно заданном project directory.

Опасные признаки:

- абсолютный путь, ведущий в `/srv/supabase/old-project`;
- `external: true` с volume старого проекта;
- явно заданный `name:` volume, совпадающий со старым;
- общий каталог Storage;
- ручной `container_name`, совпадающий с существующим контейнером.

Если в Compose есть явные имена, они должны быть уникальными:

```yaml
volumes:
  db-data:
    name: supabase_cybergym_db_data
```

Однако предпочтительнее убрать `name:` и позволить Compose автоматически добавить prefix `supabase_cybergym_`.

## 9. Генерация новых секретов

Нельзя копировать `.env` старого проекта целиком. Для нового проекта генерируются новые значения:

- `POSTGRES_PASSWORD`;
- `JWT_SECRET`;
- publishable/anon key;
- secret/service-role key;
- `SECRET_KEY_BASE`;
- `VAULT_ENC_KEY`;
- dashboard username/password;
- pooler tenant ID и пароли;
- любые webhook, SMTP и OAuth secrets.

В актуальной официальной Docker-сборке используются штатные скрипты:

```bash
cd /srv/supabase/cybergym
sh utils/generate-keys.sh
sh utils/add-new-auth-keys.sh
```

После генерации:

```bash
chmod 600 .env
```

Не запускать стек с placeholder-значениями из `.env.example`. Никогда не помещать `SERVICE_ROLE_KEY`, secret key или Postgres-пароль в Android-приложение. Клиентскому приложению разрешены только публичный URL и publishable/anon key.

## 10. URL нового проекта

Пример значений нового `.env`:

```dotenv
SUPABASE_PUBLIC_URL=https://supabase.cybergym.ru
API_EXTERNAL_URL=https://supabase.cybergym.ru
SITE_URL=https://app.cybergym.ru
```

Значения Auth redirect URL и OAuth callback URL должны соответствовать реальным доменам приложения. Для Android deep links добавляются только необходимые разрешённые redirect URI.

## 11. Разведение портов

Два контейнера не могут одновременно публиковать один host port на одном IP. Если старый gateway занимает `8000`, новый можно временно привязать только к localhost:

```yaml
services:
  kong:
    ports:
      - "127.0.0.1:8100:8000"
```

Имена сервисов и внутренние порты следует сверять с используемой версией официального Compose.

Для БД и pooler можно использовать, например:

```text
старый direct/pooler: 5432 / 6543
новый direct/pooler:  5433 / 6544
```

Но Postgres лучше не публиковать в интернет. Если доступ снаружи необходим для администрирования, использовать `127.0.0.1` и SSH-туннель:

```bash
ssh -L 5433:127.0.0.1:5433 user@vps.example.com
```

Проверка итоговых портов до запуска:

```bash
docker compose -p supabase_cybergym config > /tmp/cybergym-compose.rendered.yml
grep -n -A8 -B2 'published:' /tmp/cybergym-compose.rendered.yml
sudo ss -lntup
```

Ни один опубликованный порт нового Compose не должен конфликтовать с существующим listener.

## 12. Reverse proxy и HTTPS

Предпочтительная схема — Nginx или Caddy принимает запросы на `443`, завершает TLS и проксирует их на локальный API gateway нового Supabase.

Пример Nginx для нового проекта:

```nginx
server {
    listen 80;
    server_name supabase.cybergym.ru;

    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name supabase.cybergym.ru;

    ssl_certificate     /etc/letsencrypt/live/supabase.cybergym.ru/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/supabase.cybergym.ru/privkey.pem;

    client_max_body_size 50m;

    location / {
        proxy_pass http://127.0.0.1:8100;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_read_timeout 3600;
        proxy_send_timeout 3600;
    }
}
```

Перед reload:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

Для Caddy аналогичная базовая конфигурация:

```caddyfile
supabase.cybergym.ru {
    reverse_proxy 127.0.0.1:8100
}
```

Не заменяйте существующий конфигурационный файл reverse proxy целиком. Добавляйте отдельный virtual host и обязательно проверяйте конфигурацию перед reload.

## 13. DNS и сертификат

Создать DNS-запись:

```text
Type: A
Name: supabase.cybergym.ru
Value: PUBLIC_IP_VPS
```

Если используется IPv6, также проверить `AAAA`. До выпуска сертификата убедиться, что запись уже указывает на VPS:

```bash
dig +short supabase.cybergym.ru A
dig +short supabase.cybergym.ru AAAA
```

Порты `80/443` должны оставаться открытыми только для reverse proxy. Порты Postgres, Studio admin interfaces и внутренние служебные порты не следует открывать всему интернету.

## 14. Проверка Compose до первого запуска

```bash
cd /srv/supabase/cybergym
docker compose -p supabase_cybergym config --quiet
docker compose -p supabase_cybergym config \
  > /tmp/cybergym-compose.rendered.yml
```

Проверить в отрендеренном файле:

- имя проекта отличается от старого;
- нет путей старого проекта;
- нет старых named volumes;
- нет старых `container_name`;
- нет конфликтующих host ports;
- публичные URL относятся к новому домену;
- отсутствуют placeholder-пароли;
- база и Storage используют новые каталоги.

Найти подозрительные ссылки:

```bash
grep -n '/srv/supabase/old-project' /tmp/cybergym-compose.rendered.yml
grep -n 'CHANGE_ME\|your-super-secret\|placeholder' \
  /tmp/cybergym-compose.rendered.yml
```

Отсутствие вывода для этих команд — ожидаемый результат.

## 15. Первый запуск

```bash
cd /srv/supabase/cybergym
docker compose -p supabase_cybergym up -d
docker compose -p supabase_cybergym ps
docker compose -p supabase_cybergym logs --tail=200
```

Параллельно убедиться, что старый проект продолжает работать:

```bash
docker compose ls
curl -fsS https://supabase.old-domain.ru/rest/v1/ >/dev/null
curl -fsS https://supabase.cybergym.ru/rest/v1/ >/dev/null
```

Ответ `401`, `403` или сообщение о недостающем API key может быть нормальным: главное, что DNS, TLS и gateway отвечают. Для полноценной проверки нужно передать публичный API key.

## 16. Проверки нового Supabase

Проверить:

1. Studio открывается только через защищённый доступ.
2. REST API отвечает с publishable/anon key.
3. Регистрация и вход Auth работают.
4. Письма уходят через production SMTP.
5. Realtime устанавливает WebSocket-соединение.
6. Storage позволяет создать bucket и загрузить тестовый файл.
7. Row Level Security включена для таблиц с пользовательскими данными.
8. Android-приложение использует только новый URL и публичный ключ.
9. Старый проект продолжает проходить свои smoke tests.

## 17. Подключение Android-приложения CyberGym

В репозитории реальные значения хранятся локально и не коммитятся. Пример `local.properties`:

```properties
SUPABASE_URL=https://supabase.cybergym.ru
SUPABASE_ANON_KEY=PUBLIC_CLIENT_KEY_HERE
```

В репозиторий можно добавлять только шаблон без секретов:

```properties
SUPABASE_URL=
SUPABASE_ANON_KEY=
```

Никогда не добавлять в Android APK:

- `SERVICE_ROLE_KEY` или Supabase secret key;
- `POSTGRES_PASSWORD`;
- JWT signing secret;
- dashboard credentials;
- SMTP password.

Публичный anon/publishable key сам по себе не заменяет безопасность. Доступ к данным должен ограничиваться RLS-политиками в Postgres.

## 18. Остановка только нового проекта

```bash
cd /srv/supabase/cybergym
docker compose -p supabase_cybergym stop
```

Повторный запуск:

```bash
docker compose -p supabase_cybergym start
```

Удаление контейнеров и сети без удаления данных:

```bash
docker compose -p supabase_cybergym down
```

Команда `down -v` запрещена для обычного обслуживания, потому что удаляет named volumes проекта.

## 19. Обновление Supabase

Нельзя обновлять оба проекта одновременно без необходимости. Для каждого стека:

1. прочитать changelog целевой версии;
2. сделать дамп базы и резервную копию Storage;
3. сохранить текущие Compose-файлы и `.env`;
4. сравнить новый `.env.example` с рабочим `.env`;
5. проверить изменения миграций и образов;
6. обновить сначала менее критичный проект;
7. выполнить smoke tests;
8. только затем планировать обновление второго проекта.

Не использовать плавающий `latest` без контролируемого процесса обновления. По возможности фиксировать версии образов.

## 20. Резервное копирование нового проекта

Минимальная production-политика должна включать:

- ежедневный логический дамп Postgres;
- резервное копирование Storage;
- копию Compose-конфигурации и зашифрованную копию `.env`;
- хранение копий вне самого VPS;
- ротацию резервных копий;
- мониторинг успешности backup job;
- регулярное тестовое восстановление.

Резервная копия на том же диске защищает от ошибки приложения, но не от отказа VPS или диска.

## 21. Мониторинг

Минимально контролировать:

- свободное место и inode;
- RAM и swap;
- restart count контейнеров;
- состояние Postgres;
- HTTP health checks обоих доменов;
- срок TLS-сертификатов;
- размер базы и Storage;
- успешность резервных копий;
- ошибки Auth, REST, Realtime и Storage.

Полезные команды:

```bash
docker compose -p supabase_cybergym ps
docker compose -p supabase_cybergym logs --since=30m
docker inspect -f '{{.Name}} {{.RestartCount}}' $(docker ps -q)
docker system df
```

`docker system prune` не должен быть частью автоматического обслуживания без точного понимания, какие ресурсы будут удалены.

## 22. План отката

Если новый проект мешает старому:

```bash
cd /srv/supabase/cybergym
docker compose -p supabase_cybergym stop
```

Затем:

1. проверить состояние старого домена;
2. откатить только новый virtual host reverse proxy, если проблема в нём;
3. снова проверить конфигурацию Nginx/Caddy;
4. изучить логи нового стека;
5. исправить конфликт порта, памяти, диска или пути volume;
6. не удалять volumes до выяснения причины.

Если новый стек исчерпал RAM, остановить его и проверить OOM-события:

```bash
dmesg -T | grep -i -E 'out of memory|oom|killed process'
```

## 23. Финальный чек-лист

До запуска:

- [ ] Создана и проверена резервная копия старой БД.
- [ ] Скопирован старый Storage и конфигурация.
- [ ] Известно Compose project name старого проекта.
- [ ] Новый проект находится в отдельном каталоге.
- [ ] У нового проекта уникальный `COMPOSE_PROJECT_NAME`.
- [ ] Все bind mounts ведут в новый каталог.
- [ ] Нет общих или совпадающих named volumes.
- [ ] Сгенерированы новые секреты.
- [ ] Новый `.env` имеет права `600` и не хранится в Git.
- [ ] Host-порты не конфликтуют.
- [ ] Postgres не открыт всему интернету.
- [ ] DNS нового домена указывает на VPS.
- [ ] Reverse proxy config прошёл проверку.
- [ ] На сервере достаточно RAM и диска.

После запуска:

- [ ] Все контейнеры нового проекта healthy/running.
- [ ] Старый Supabase продолжает работать.
- [ ] TLS нового домена корректен.
- [ ] REST/Auth/Realtime/Storage проверены.
- [ ] Включены и проверены RLS-политики.
- [ ] Android-приложение не содержит серверных секретов.
- [ ] Настроены отдельные backups и мониторинг.

## Официальные источники

- [Supabase: Self-Hosting with Docker](https://supabase.com/docs/guides/self-hosting/docker)
- [Supabase: Self-hosted Envoy и reverse proxy](https://supabase.com/docs/guides/self-hosting/self-hosted-envoy)
- [Supabase: новые API-ключи и asymmetric authentication](https://supabase.com/docs/guides/self-hosting/self-hosted-auth-keys)
- [Docker Compose: project name](https://docs.docker.com/compose/how-tos/project-name/)
- [Docker Compose: volumes](https://docs.docker.com/reference/compose-file/volumes/)

