# LightBoard

Минималистичный full-stack дашборд для анализа и визуализации CSV-файлов.

## Технологический стек

### Фронтенд
- React 18 + TypeScript
- Vite + Bun
- Современный адаптивный UI

### Бэкенд
- Java 21
- Spring Boot 3.x
- PostgreSQL 15

### Инфраструктура
- Docker & Docker Compose
- Режим разработки с горячей перезагрузкой

## Быстрый старт

### Предварительные требования
- Установленные Docker & Docker Compose
- Доступные порты 3000, 8080 и 5432

### Запуск приложения

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd LightBoard
```

2. Запустите все сервисы:
```bash
docker-compose up --build
```

3. Откройте приложение:
- **Фронтенд**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/hello
- **PostgreSQL**: localhost:5432

### Первый запуск

При первом запуске вы увидите:
- Инициализацию базы данных PostgreSQL
- Запуск Spring Boot приложения (бэкенд)
- Запуск Vite dev-сервера с горячей перезагрузкой (фронтенд)

Фронтенд отобразит сообщение "Hello from LightBoard Backend!" когда все сервисы будут подключены.

## Разработка

### Горячая перезагрузка

Оба сервиса (фронтенд и бэкенд) настроены для горячей перезагрузки:

- **Фронтенд**: Редактируйте файлы в `frontend/src/` и видите изменения немедленно
- **Бэкенд**: Редактируйте Java-файлы в `backend/src/` и Spring DevTools автоматически перезапустит приложение

### Остановка сервисов

```bash
docker-compose down
```

Для удаления volumes (данных базы):
```bash
docker-compose down -v
```

### Просмотр логов

Все сервисы:
```bash
docker-compose logs -f
```

Конкретный сервис:
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

## API Endpoints

### Текущие эндпоинты

- `GET /api/hello` - Эндпоинт проверки здоровья, возвращает статус и временную метку

## Следующие шаги

Текущая настройка предоставляет минимальное рабочее окружение. См. `docs/Full Stack Architecture Plan.md` для полной информации об архитектуре.
