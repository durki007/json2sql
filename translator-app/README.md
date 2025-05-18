# Example prompts
1. Basic `SELECT`
/POST http://localhost:8080/users
```json
{
"queryType": "SELECT",
"table": "user_entity",
"columns": ["id", "first_name", "email", "last_name"]
}
```

2. `WHERE` condition
/POST http://localhost:8080/users
```json
{
  "queryType": "SELECT",
  "table": "user_entity",
  "columns": ["id", "first_name", "email", "last_name"],
  "conditions": [
    { "column": "email", "operator": "LIKE", "value": "%gmail.com%" }
  ]
}
```

3. `ORDER BY`
/POST http://localhost:8080/users
```json
{
  "queryType": "SELECT",
  "table": "user_entity",
  "columns": ["id", "first_name", "email", "last_name"],
  "orderBy": [
    { "column": "first_name", "direction": "ASC" }
  ]
}
```

4. `INSERT`
/POST http://localhost:8080/users
```json
{
  "queryType": "INSERT",
  "table": "user_entity",
  "columns": ["first_name", "last_name", "email"],
  "values": [
    {
      "first_name": "Alice",
      "last_name": "Johnson",
      "email": "alice.johnson@example.com"
    }
  ]
}
```

5. `UPDATE`
/POST http://localhost:8080/users
```json
{
  "queryType": "UPDATE",
  "table": "user_entity",
  "columns": ["first_name"],
  "values": [
    { "first_name": "Mark" }
  ],
  "conditions": [
    { "column": "first_name", "operator": "=", "value": "Alice" }
  ]
}
```

6. `DELETE`
/POST http://localhost:8080/users
```json
{
  "queryType": "DELETE",
  "table": "user_entity",
  "conditions": [
    { "column": "email", "operator": "LIKE", "value": "%spam.com" }
  ]
}
```

7. `LIMIT`
/POST http://localhost:8080/users
```json
{
  "queryType": "SELECT",
  "table": "user_entity",
  "columns": ["id", "first_name", "email", "last_name"],
  "limit": 2
}
```

Currently `GROUP BY`, `AVG`, `MAX`, `MIN`, `COUNT()` and `JOIN` operations are not supported

## Running integration tests

1. run all tests
```shell
cd translator-app
mvn test
```


2. run specific test
```shell
cd translator-app
mvn test -Dtest=UserControllerTest
```
