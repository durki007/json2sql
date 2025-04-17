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
