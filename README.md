# MIASI Project

This project consists of two modules:
1. **json2sql-lib**: A library for converting JSON to SQL.
2. **translator-app**: A Spring Boot application that uses the `json2sql-lib` library.

## Prerequisites

Before running the project, ensure you have the following installed:
- [Java 21](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) (for running the PostgreSQL database)

## Build and Run Instructions

### Step 1: Clone the Repository
Clone the repository to your local machine:
```bash
git clone <repository-url>
cd miasi
```

### Step 2: Run the Database Using Docker Compose
Start the PostgreSQL database using `docker-compose`:
```bash
docker-compose up -d
```

This will start a PostgreSQL container with the following configuration:
- **POSTGRES_USER**: `translator_user`
- **POSTGRES_PASSWORD**: `translator_password`
- **POSTGRES_DB**: `translator_db`
- **Port Mapping**: Maps the container's port `5432` to the host's port `5432`.

### Step 3: Configure the Application
Ensure the `application.properties` file in the `translator-app` module is configured to connect to the database:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/translator_db
spring.datasource.username=translator_user
spring.datasource.password=translator_password
```

### Step 4: Build the Project
Build the `json2sql-lib` module and install it to the local Maven repository:
```bash
mvn clean install -pl json2sql-lib
```

### Step 5: Run the Translator App
Run the `translator-app` module using the Spring Boot Maven plugin:
```bash
mvn spring-boot:run -pl translator-app
```

### Step 6: Access the Application
Once the application starts, you can access it at:
```
http://localhost:8080
```

## Project Structure

```
miasi/
├── json2sql-lib/       # Library module for JSON to SQL conversion
├── translator-app/     # Spring Boot application
├── docker-compose.yml  # Docker Compose configuration for PostgreSQL
├── pom.xml             # Parent POM file
└── README.md           # Project documentation
```

## Notes
- Ensure that the database configuration in `translator-app` (e.g., `application.properties`) matches the `docker-compose.yml` settings.
- To stop the database container, run:
  ```bash
  docker-compose down
  ```
- If you encounter any issues, check the logs for detailed error messages.
