# FinOps Backend Foundation

This milestone creates only the Spring Boot foundation and Actuator health endpoint. It contains no entities, repositories, authentication, AWS integration, ML code, or database schema changes.

## Requirements

- Java 21
- Maven 3.9+
- MySQL 8+ when testing the `database` profile

## Run the basic health check

The default configuration has no datasource. It is intentionally runnable without inventing a local database or altering the frozen schema.

```powershell
mvn spring-boot:run
Invoke-RestMethod http://localhost:8080/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

## Verify the real frozen database connection

Obtain the actual database vendor and connection settings from the project owner before running this command. No schema file was found in this workspace, so the configuration does not guess table or column names.

The supplied frozen schema is MySQL-specific (`AUTO_INCREMENT`, `ENUM`, and `USE`), so use MySQL:

```powershell
$env:SPRING_PROFILES_ACTIVE = "database"
$env:DB_URL = "jdbc:mysql://host:3306/finops_assistant"
$env:DB_USERNAME = "database_user"
$env:DB_PASSWORD = "database_password"
$env:DB_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver"
mvn -Pdatabase spring-boot:run
```

With the `database` profile active, a failed or unavailable datasource makes `/actuator/health` report `DOWN`; a successful connection is reported through the standard Actuator database health contributor.

Hibernate DDL generation and SQL initialization are both disabled in this profile to preserve the frozen schema.
