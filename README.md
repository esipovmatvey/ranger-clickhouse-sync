# Ranger ClickHouse Sync

Сервис синхронизации политик безопасности Apache Ranger → ClickHouse и выгрузки событий аудита в Quickwit.

## Требования перед запуском

- В Ranger должен быть создан экземпляр сервиса ClickHouse.
- Заполнены параметры подключения в `application.yml`.

## Конфигурация

Все настройки задаются в `application.yml`. Ниже перечислены обязательные параметры.

### Ranger

ranger:
    url — URL Ranger Admin
    username — логин для доступа к Ranger Admin
    password — пароль для доступа к Ranger Admin
    service-name — имя сервиса ClickHouse, зарегистрированного в Ranger
    policies-endpoint — endpoint для получения политик (не требует изменения)
    sync:
        interval-ms — интервал синхронизации политик в миллисекундах (по умолчанию 60000)
    audit:
        repo-type — числовой код типа репозитория
        sync:
            interval-ms — интервал синхронизации аудита в миллисекундах (по умолчанию 60000)

### ClickHouse

clickhouse:
    url — JDBC URL для подключения к ClickHouse
    username — имя пользователя ClickHouse
    password — пароль пользователя ClickHouse

### PostgreSQL

postgres:
    url — JDBC URL для подключения к PostgreSQL (схема sync будет создана автоматически)
    username — имя пользователя PostgreSQL
    password — пароль пользователя PostgreSQL

### Quickwit

quickwit:
    url — базовый URL Quickwit API
    index — имя индекса для хранения событий аудита (по умолчанию ranger_audits)
    index-endpoint — endpoint для управления индексами (по умолчанию /api/v1/indexes)
    ingest-endpoint — endpoint для загрузки данных (по умолчанию /api/v1/{index}/ingest)


## Миграции базы данных

Сервис использует Flyway для автоматического создания таблиц в схеме sync, указанной в postgres.url. При первом запуске создаются:

- sync.ranger_sync_state — хранение состояния синхронизации политик
- sync.audit_state — хранение метки времени последней выгрузки аудита

## Сборка и запуск

- Убедитесь, что конфигурация в application.yml заполнена корректно.
- Выполните сборку проекта: `mvn clean install -DskipTests`
- Запустите приложение: `java -jar <путь до jar>`
