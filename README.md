# Многопоточный агрегатор данных из REST API (Этап 1)

Курсовая работа по дисциплине «Объектно-ориентированное программирование».
**Этап 1**: консольное приложение, которое опрашивает несколько открытых API, приводит ответы к единой модели и сохраняет их в файл (JSON или CSV), а также умеет читать данные обратно.

## 💻 Используемые технологии

- **Java 17** (включая `java.time` и `java.net.http.HttpClient`)
- **Maven** (система сборки)
- **Jackson** (библиотека для парсинга и работы с JSON)
- **Apache Commons CSV** (работа с CSV-файлами)
- **SLF4J + Logback** (логирование)
- **Java NIO** (работа с файловой системой: `Files`, `Path`)

## 🌐 Выбранные API

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

## 🚀 Сборка и запуск

### Требования
- Java 17+
- Maven 3.8+

### Сборка
```bash
mvn clean compile
```

### Запуск опроса (poll)
```bash
mvn exec:java -Dexec.args="poll --sources=<список> --format=<json|csv> --mode=<create|append>"
```

### Чтение данных (print)
```bash
# Вывести всё
mvn exec:java -Dexec.args="print"

# Вывести только данные от Open-Meteo
mvn exec:java -Dexec.args="print --source=open-meteo"
```

### Справка (help)
```bash
mvn exec:java -Dexec.args="help"
```


---

## Архитектура и формат

### Архитектура проекта

Проект разделен на слои для соблюдения принципов ООП и разделения ответственности:

- `cli` — консольный интерфейс (парсинг аргументов `CommandParser`, обработка команд `CommandLineApp`, модель команды `Command`).
- `aggregation` — бизнес-логика (оркестратор `Aggregator`, который опрашивает источники и передает данные в хранилище).
- `source` — источники данных (интерфейс `DataSource`, базовый класс `AbstractDataSource` и реализации для каждого API).
- `http` — работа с сетью (интерфейс `HttpClientWrapper` и его реализация `JavaHttpClientWrapper`).
- `storage` — хранилище (интерфейс `DataStorage`, реализации `JsonDataStorage`, `CsvDataStorage` и `StorageFactory`).
- `model` — единая модель данных (`AggregatedData`, `WriteMode`).
- `config` — сборка объектов (класс `AppConfig` — Composition Root).

### Ключевые особенности:
- **Единая модель:** Все ответы API приводятся к классу `AggregatedData`.
- **Шаблонный метод (Template Method):** `AbstractDataSource` реализует общий скелет HTTP-запроса и парсинга JSON, оставляя наследникам только специфику API.
- **Фабрики и реестры:** `StorageFactory` создаёт хранилище нужного формата, `DataSourceRegistry` хранит все доступные источники.
- **Обработка ошибок:** Каждый слой имеет своё unchecked-исключение (`CliException`, `HttpClientException`, `DataSourceException`, `StorageException`). Сетевые вызовы и ошибки парсинга не прерывают опрос остальных источников (устойчивость к сбоям).
- **Логирование:** Используется SLF4J + Logback.

---

## 📊 Формат данных

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