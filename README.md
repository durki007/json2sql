# Translator Application

This project consists of a translator application and a PostgreSQL database. The services are orchestrated using Docker Compose.

## Prerequisites

- Install [Docker](https://www.docker.com/)
- Install [Docker Compose](https://docs.docker.com/compose/)
- Install [Maven](https://maven.apache.org/) (for development mode)
- Install [Java 21](https://jdk.java.net/21/) (for development mode)

## Running the Project

### 1. Demonstration Mode (Full Docker Compose)

1. Clone the repository to your local machine:
   ```bash
   git clone <repository-url>
   cd <repository-folder>
   ```

2. Start the services using Docker Compose:
   ```bash
   docker-compose up -d
   ```

   This will:
   - Start a PostgreSQL database container (`postgres-db`) on port `5432`.
   - Build and start the translator application container (`translator-app`) on port `8080`.

3. Verify the services:
   - PostgreSQL: Connect to `localhost:5432` using a PostgreSQL client with the following credentials:
     - Username: `translator_user`
     - Password: `translator_password`
     - Database: `translator_db`
   - Translator App: Access the application at `http://localhost:8080`.

4. To stop the services:
   ```bash
   docker-compose down
   ```

### 2. Development Mode (Database Dockerized, App Run Locally)

1. Clone the repository to your local machine:
   ```bash
   git clone <repository-url>
   cd <repository-folder>
   ```

2. Start only the PostgreSQL database using Docker Compose:
   ```bash
   docker-compose up -d postgres-db
   ```

   This will start the PostgreSQL database container (`postgres-db`) on port `5432`.

3. Build the library module:
   ```bash
   mvn clean install -pl json2sql-lib -am
   ```

   This will build the `json2sql-lib` module and install it in the local Maven repository.

4. Configure the application to connect to the database:
   - Ensure the following environment variables are set in your local environment:
     - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/translator_db`
     - `SPRING_DATASOURCE_USERNAME=translator_user`
     - `SPRING_DATASOURCE_PASSWORD=translator_password`

5. Run the Spring application locally:
   ```bash
   mvn spring-boot:run -pl translator-app
   ```

   The application will be accessible at `http://localhost:8080`.

6. To stop the database container:
   ```bash
   docker-compose down
   ```

## Notes

- The database data is persisted in a Docker volume named `postgres_data`.
- Ensure that the `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` environment variables are correctly implemented in the application.


## Interacting with the application

For guidelines how to interact with the app check the `README.md` file in the `./translator-app`.