# План архитектуры full‑stack приложения для минималистичного дашборда визуализации данных

## 1. Обзор

Приложение позволяет:


- **React 18 + TypeScript**
- **Vite + Bun**
- **Tailwind CSS** — утилитарная стилизация с компактным CSS.
- **Recharts** — основная библиотека графиков.
- **React Query** — управление серверным состоянием (datasets, chart‑data, upload‑status).
- **React Router** — маршрутизация (`/`, `/datasets`, `/upload/:jobId`, `/dashboard/:datasetId`).
- **Axios** — HTTP‑клиент.

### 2.2 Бэкенд

- **Язык:** Java 21+.
- **Фреймворк:** Spring Boot 3.x.
- **Модули:** Spring Web, Spring Data JPA, WebSocket (STOMP или simple broker), Validation, Spring Boot Actuator.
- **БД:** PostgreSQL 15.
- **ORM:** Spring Data JPA + ручной SQL через `JdbcTemplate` для динамических таблиц.
- **Парсер CSV:** OpenCSV (стриминговый режим).
- **Асинхронность:** `@Async` + `TaskExecutor` / `CompletableFuture` для фоновой обработки загрузки.

### 2.3 Инфраструктура

- **Docker Compose** с тремя сервисами:
  - `frontend` — Nginx, отдающий собранный React‑бандл;
  - `backend` — Spring Boot приложение;
  - `postgres` — БД PostgreSQL 15.
- Один bridge‑network для всех сервисов.
- Именованный volume для данных Postgres.

---

## 3. Архитектура backend‑а

### 3.1 Структура проекта

```text
backend/
├── src/main/java/com/dashboard/
│   ├── DashboardApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   └── WebSocketConfig.java
│   ├── controller/
│   │   ├── UploadController.java
│   │   ├── DatasetController.java
│   │   └── ChartController.java
│   ├── service/
│   │   ├── CsvProcessingService.java
│   │   ├── DatasetService.java
│   │   ├── ChartDataService.java
│   │   └── SamplingService.java
│   ├── database/
│   │   ├── DatasetRepository.java
│   │   ├── DatasetColumnRepository.java
│   │   └── DynamicQueryRepository.java
│   ├── model/
│   │   ├── Dataset.java
│   │   ├── DatasetColumn.java
│   │   └── UploadJob.java
│   └── dto/
│       ├── UploadResponse.java
│       ├── JobStatusResponse.java
│       └── ChartDataRequest.java
├── src/main/resources/
│   ├── application.yml
│   └── application-dev.yml
└── build.gradle
```

Слои:

- **controller** — HTTP/WebSocket‑контроллеры без бизнес‑логики;
- **service** — бизнес‑правила: парсинг CSV, работа с датасетами, агрегация и сэмплирование;
- **database** — доступ к БД через JPA и `JdbcTemplate`;
- **model/dto** — доменные сущности и структуры запросов/ответов;

### 3.2 Схема БД

Минимальный набор системных таблиц:

- `datasets` — регистр наборов данных:
  - `id`, `name`, `original_filename`, `row_count`, `column_count`,
  - `processing_status` (PENDING/PROCESSING/COMPLETED/FAILED),
  - `table_name` (имя динамической таблицы),
  - таймстемпы и краткое описание ошибки.

- `dataset_columns` — метаданные колонок:
  - `dataset_id`, `column_name`, `column_index`,
  - `detected_type` (NUMERIC, DATE, DATETIME, CATEGORICAL, TEXT, BOOLEAN),
  - `postgres_type`,
  - sample‑значения и min/max (для числовых/дат).

- `upload_jobs` — статус фоновой обработки:
  - `id` (`jobId`), `dataset_id` (опционально, пока не создан),
  - `job_status` (QUEUED/PROCESSING/COMPLETED/FAILED),
  - `progress_percent`,
  - счётчики строк (total, processed, skipped),
  - текущая фаза (`PARSING`, `TYPE_DETECTION`, `TABLE_CREATION`, `DATA_INSERTION`),
  - `error_details`.

Для каждого загруженного CSV создаётся отдельная таблица `data_<dataset_uuid>` с колонками под конкретный файл и индексами по датам/категориальным полям.

### 3.3 Поток обработки загрузки CSV

1. Клиент отправляет `POST /api/datasets/upload` с multipart‑телом (файл + опциональные метаданные).
2. `UploadController`:
   - сохраняет файл во временный каталог;
   - создаёт запись в `upload_jobs` со статусом `QUEUED`;
   - возвращает `{ jobId }`.
3. `CsvProcessingService` запускается асинхронно (`@Async`) и:
   - определяет кодировку и разделитель на основе первых N строк;
   - читает файл стримингово (без загрузки целиком в память);
   - собирает sample‑значения по каждой колонке;
   - определяет типы колонок (NUMERIC/DATE/DATETIME/BOOLEAN/CATEGORICAL/TEXT);
   - создаёт запись `Dataset` и динамическую таблицу `data_<dataset_uuid>`;
   - вставляет данные батчами (например, по 1000 строк), обновляя прогресс в `upload_jobs`;
   - считает статистику (общее число, валидные/пропущенные строки).
4. При ошибке:
   - логирует подробности;
   - устанавливает статус `FAILED` и краткое описание в `error_details`.

Фронтенд опрашивает `GET /api/upload/{jobId}/status` либо подписывается на WebSocket‑канал для получения прогресса в реальном времени.

### 3.4 Запрос данных для визуализаций

`ChartDataService`:

- валидирует:
  - наличие датасета и запрошенных колонок;
  - совместимость типа графика и типов колонок;
- строит параметризованный SQL на динамической таблице:
  - `WHERE` по датам, категориям и числовым диапазонам;
  - `GROUP BY` по выбранным измерениям;
  - агрегаты `SUM`, `AVG`, `COUNT`, `MIN`, `MAX`;
  - bucketing по времени (день/неделя/месяц) для time‑series;
- при необходимости делегирует в `SamplingService` (слишком много точек).

Результат приводится к удобной для Recharts форме: массив объектов вида

```json
{ "x": "...", "value": 123, "seriesA": 10, "seriesB": 20 }
```

или двумерные матрицы для heatmap.

---

## 4. Архитектура frontend‑а

### 4.1 Структура проекта

```text
frontend/
├── src/
│   ├── components/
│   │   ├── charts/        # BarChart, PieChart
│   │   ├── upload/        # CSVUploader, UploadProgress
│   │   ├── datasets/      # DatasetList, DatasetCard
│   │   └── layout/        # AppLayout, Sidebar, Header
│   ├── hooks/             # useDatasets, useUpload, useChartData
│   ├── services/          # apiClient, datasetService, chartService
│   ├── router/            # определение маршрутов
│   ├── types/             # типы DTO и ответов API
│   ├── App.tsx
│   └── main.tsx
└── vite.config.ts
```

Основные страницы:

- `/` — приветственный экран + блок загрузки файла;
- `/datasets` — список датасетов с поиском, сортировкой и действиями;
- `/upload/:jobId` — статус обработки конкретной загрузки;
- `/dashboard/:datasetId` — дашборд с набором графиков и фильтров по выбранному датасету.

### 4.2 Работа с состоянием

- Все вызовы API обёрнуты в React Query (`useQuery` / `useMutation`).
- Текущий датасет и применённые фильтры хранятся в локальном состоянии компонентов и/или простом React Context.
- WebSocket‑подписки инкапсулированы в `useWebSocket` и при событиях вызывают `queryClient.invalidateQueries` для соответствующих ключей.
- Глобальные state‑менеджеры (Redux, Zustand) не используются.

---

## 5. API и WebSocket

### 5.1 REST API (минимальный набор)

- `POST /api/datasets/upload` — загрузка CSV, ответ `{ jobId }`.
- `GET /api/upload/{jobId}/status` — статус, прогресс, краткая статистика.
- `GET /api/datasets` — список датасетов (пагинация, фильтрация по статусу — опционально).
- `GET /api/datasets/{id}` — метаданные датасета.
- `GET /api/datasets/{id}/columns` — список колонок и их типов.
- `GET /api/datasets/{id}/preview` — первые N строк (с пагинацией).
- `DELETE /api/datasets/{id}` — удаление датасета и связанной таблицы.
- `POST /api/charts/data` — универсальная точка для получения данных для любого графика (тип графика, выбранные колонки, фильтры, доп. параметры).

Ответы API должны быть пригодны напрямую для Recharts, чтобы минимизировать трансформации на фронтенде.

---

## 6. Docker‑архитектура и окружения

### 6.1 Docker Compose

Три сервиса:

- **frontend**
  - multi‑stage Dockerfile: сборка Vite (Bun/Node) → лёгкий Nginx;
  - SPA‑роутинг (все запросы → `index.html`);
  - proxy `/api` на backend.

- **backend**
  - multi‑stage Dockerfile: Gradle + JDK для сборки → JRE‑runtime;
  - порт `8080`;
  - переменные окружения для подключения к Postgres и выбора профиля Spring.

- **postgres**
  - официальный образ PostgreSQL 15;
  - именованный volume для данных;
  - `pg_isready` в health‑check.

Сервисы объединены в один bridge‑network, снаружи торчат только порты фронтенда и (по необходимости) backend‑а.

### 6.2 Окружения

- **dev**
  - включён hot‑reload фронтенда (Vite);
  - Spring DevTools для автоперезапуска backend‑а;
  - auto‑DDL для ускорения разработки.

