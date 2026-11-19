# Агрегатор данных из REST API (Этап 2)

Курсовая работа по дисциплине «Объектно-ориентированное программирование».
**Этап 2**: Spring Boot приложение, которое параллельно опрашивает несколько открытых REST API, приводит ответы к единой модели, сохраняет их в файл (JSON или CSV) и управляется через собственный REST API.

## Используемые технологии

- **Java 17** (включая `java.time` и `java.net.http.HttpClient`)
- **Spring Boot 3.3.2** (REST API, DI, конфигурация)
- **Maven** (система сборки)
- **Jackson** (библиотека для парсинга и работы с JSON)
- **Apache Commons CSV** (работа с CSV-файлами)
- **SLF4J + Logback** (логирование)
- **JUnit 5 + Mockito** (тестирование)
- **JaCoCo** (покрытие кода тестами ≥ 70%)
- **Java NIO** (работа с файловой системой: `Files`, `Path`)

## Выбранные API

В проекте используются три открытых API, не требующих ключей авторизации:

1. **Open-Meteo** (`open-meteo`)
    - *Категория:* weather
    - *Описание:* Погода. Возвращает текущую температуру, скорость ветра и почасовой прогноз.
2. **Open Library** (`open-library`)
    - *Категория:* book
    - *Описание:* Библиотека книг. Поиск по названию, возвращает авторов, год издания и теги.
3. **World Bank** (`world-bank`)
    - *Категория:* country
    - *Описание:* Данные Всемирного банка о странах. Возвращает столицу, регион, уровень дохода и координаты.

Все API удовлетворяют требованиям задания: JSON-ответ, минимум 5 полей, наличие вложенных объектов/массивов, поддержка query-параметров, данные зависят от параметров.

---

## Архитектура и формат

### Архитектура проекта

Проект разделен на слои для соблюдения принципов ООП и разделения ответственности:

- `source` — источники данных (интерфейс `DataSource`, базовый класс `AbstractDataSource` и реализации для каждого API).
- `http` — работа с сетью (интерфейс `HttpClientWrapper` и его реализация `JavaHttpClientWrapper`).
- `storage` — хранилище (интерфейс `DataStorage`, реализации `JsonDataStorage`, `CsvDataStorage` и `StorageFactory`).
- `model` — единая модель данных (`AggregatedData`, `WriteMode`).
- `scheduling` — многопоточность (`PollingScheduler`, `PollingTask`, `PollingConfig`).
- `rest` — REST API (`PollingController`, `DataController`, DTO).
- `config` — сборка бинов (`AppConfig` — Composition Root).

### Ключевые особенности:
- **Единая модель:** Все ответы API приводятся к классу `AggregatedData`.
- **Шаблонный метод (Template Method):** `AbstractDataSource` реализует общий скелет HTTP-запроса и парсинга JSON, оставляя наследникам только специфику API.
- **Фабрики и реестры:** `StorageFactory` создаёт хранилище нужного формата, `DataSourceRegistry` хранит все доступные источники.
- **Многопоточность:** пул потоков `Executors.newScheduledThreadPool(n)` опрашивает API параллельно.
- **Отказоустойчивость:** падение одного источника не прерывает опрос остальных (Fault Tolerance).
- **Тестирование:** покрытие JaCoCo ≥ 70%, MockMvc для контроллеров, Mockito для сетевых вызовов.
- **Логирование:** Используется SLF4J + Logback.

---

## Сборка и тестирование

### Требования
- Java 17+
- Maven 3.8+

### Сборка
```bash
mvn clean compile
```

### Тесты и проверка покрытия
```bash
mvn clean verify
```
Запускает все тесты и проверяет, что покрытие ≥ 70% (иначе сборка упадёт).
Отчёт JaCoCo: target/site/jacoco/index.html.

### Запуск приложения
```bash
mvn spring-boot:run
```

---

## 🌐 REST API

Приложение предоставляет следующие эндпоинты для управления опросом и просмотра данных.

### 1. Запуск опроса
```bash
POST /polling/start
```

**Пример запроса (curl):**
```bash
curl -X POST http://localhost:8080/polling/start \
  -H "Content-Type: application/json" \
  -d '{
    "n": 2,
    "t": 5,
    "format": "csv",
    "file": "data/custom.csv",
    "mode": CREATE,
    "sources": {
      "open-meteo": [
        { "latitude": "51.5074", "longitude": "-0.1278" }
      ],
      "world-bank": [
        { "code": "FRA" }
      ]
    }
  }'
```


Запускает параллельный опрос источников. Все поля в теле запроса необязательны. Если поле не передано, используется значение по умолчанию из `application.yml`. Если для источника передан свой список параметров, он полностью заменяет набор из конфигурации.

**Ответы:**

- 200 OK — опрос успешно запущен. Тело: {"message": "Polling started"}
- 409 Conflict — опрос уже запущен. Тело: {"message": "Polling is already running"}
- 400 Bad Request — неверные параметры (например, n <= 0).

### 2. Остановка опроса

```bash
POST /polling/stop
```

Останавливает текущий опрос, завершает потоки и освобождает ресурсы.

**Пример запроса:**

```bash
curl -X POST http://localhost:8080/polling/stop
```

**Ответы:**
- 200 OK — опрос остановлен. Тело: {"message": "Polling stopped"}
- 400 Bad Request — опрос не был запущен. Тело: {"message": "Polling is not running"}

### 3. Статус опроса

```bash
GET /polling/status
```

Возвращает текущий статус опроса.

**Пример запроса:**

```bash
curl http://localhost:8080/polling/status
```
**Ответ (JSON):**

```json
{
   "running": true,
   "n": 3,
   "t": 10,
   "activeSources": [
      "open-meteo",
      "open-library",
      "world-bank"
   ]   
}
```
running — запущен ли опрос.  
n — максимальное число одновременно выполняемых задач.  
t — интервал опроса в секундах.

### 4. Просмотр данных

```bash
GET /data
```

Возвращает содержимое файла с результатами последнего опроса. Поддерживает опциональный фильтр по источнику.

**Пример запроса (все данные):**

```bash
curl http://localhost:8080/data
```
**Пример запроса (только Open-Meteo):**

```bash
curl http://localhost:8080/data?source=open-meteo
```
**Ответ (JSON):**

```json
{
   "source": "open-meteo",
   "category": "weather",
   "key": "55.75,37.61",
   "timestamp": "2026-11-17T17:50:00Z",
   "fields": {
   "temperature": -0.2,
   "windspeed": 12.7
   }
}
```

---

## Формат данных

Каждая запись в JSON/CSV имеет единую структуру. Поле `fields` гибко хранит любые данные: числа, строки, вложенные объекты и массивы (в зависимости от источника). 
#### **Пример 1: Погода (Open-Meteo)**
```json
{
   "source": "open-meteo",
   "category": "weather",
   "key": "55.75,37.61",
   "timestamp": "2026-10-22T18:53:21Z",
   "fields": {
      "temperature": 7.0,
      "windspeed": 6.4,
      "hourly": [
         { "time": "2026-10-22T00:00", "temp": 5.3 },
         { "time": "2026-10-22T01:00", "temp": 5.1 }
      ]
   }
}
```

#### Пример 2: Книги (Open Library)
```json
{
   "source": "open-library",
   "category": "book",
   "key": "java - Head first Java",
   "timestamp": "2026-10-22T18:53:21Z",
   "fields": {
    "title": "Head first Java",
    "first_publish_year": 2003,
    "author_name": [ "Kathy Sierra" ]
   }
}
```

#### Пример 3: Страны (World Bank)
```json
{
   "source": "world-bank",
   "category": "country",
   "key": "RUS",
   "timestamp": "2026-10-22T18:53:21Z",
   "fields": {
      "name": "Russian Federation",
      "capitalCity": "Moscow",
      "region": {
      "id": "ECS",
      "value": "Europe & Central Asia"
      }
   }
}
```