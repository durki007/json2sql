package pl.pwr.parser;


import pl.pwr.antlr.JSONBaseVisitor;
import pl.pwr.antlr.JSONParser;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Json2SqlVisitor extends JSONBaseVisitor<String> {

    public Json2SqlVisitor() {
        super();
    }

    @Override
    public String visitJson(JSONParser.JsonContext ctx) {
        return visit(ctx.value());
    }

    @Override
    public String visitObj(JSONParser.ObjContext ctx) {
        String queryType = null;
        String table = null;
        List<String> columns = null;
        List<String> values = null;
        List<String> conditions = null;
        List<String> orderBy = null;
        List<String> groupBy = null;
        String limit = null;

        for (JSONParser.PairContext pair : ctx.pair()) {
            String key = pair.STRING().getText().replace("\"", "");
            String value = visitValue(pair.value());

            switch (key) {
                case "queryType" -> queryType = value != null ? value.toUpperCase() : null;
                case "table" -> table = value;
                case "columns" -> columns = value != null ? extractArray(pair.value()) : null;
                case "values" -> {
                    if (value != null) {
                        // Use different extraction methods based on query type
                        if ("INSERT".equalsIgnoreCase(queryType)) {
                            values = extractValuesForInsert(pair.value(), columns);
                        } else {
                            values = extractKeyValuePairs(pair.value());
                        }
                    }
                }
                case "conditions" -> conditions = value != null ? extractConditions(pair.value()) : null;
                case "orderBy" -> orderBy = value != null ? extractOrderBy(pair.value()) : null;
                case "groupBy" -> groupBy = value != null ? extractArray(pair.value()) : null;
                case "limit" -> limit = value;
            }
        }

        return generateSQL(queryType, table, columns, values, conditions, orderBy, groupBy, limit);
    }

    private String generateSQL(String queryType, String table, List<String> columns, List<String> values,
                               List<String> conditions, List<String> orderBy, List<String> groupBy, String limit) {
        StringBuilder sql = new StringBuilder();

        if (queryType == null || table == null) {
            return "Invalid query: missing required fields.";
        }

        switch (queryType) {
            case "SELECT" -> {
                sql.append("SELECT ")
                        .append(columns != null && !columns.isEmpty() ? String.join(", ", columns) : "*")
                        .append(" FROM ").append(table);
                if (conditions != null && !conditions.isEmpty()) sql.append(" WHERE ").append(String.join(" AND ", conditions));
                if (groupBy != null && !groupBy.isEmpty()) sql.append(" GROUP BY ").append(String.join(", ", groupBy));
                if (orderBy != null && !orderBy.isEmpty()) sql.append(" ORDER BY ").append(String.join(", ", orderBy));
                if (limit != null && !limit.isEmpty() && !"null".equals(limit)) sql.append(" LIMIT ").append(limit);
            }
            case "INSERT" -> {
                if (columns != null && !columns.isEmpty() && values != null && !values.isEmpty()) {
                    sql.append("INSERT INTO ").append(table)
                            .append(" (").append(String.join(", ", columns)).append(")")
                            .append(" VALUES (").append(String.join(", ", values)).append(")");
                } else {
                    sql.append("Invalid INSERT: columns or values are missing.");
                }
            }
            case "UPDATE" -> {
                if (values != null && !values.isEmpty()) {
                    sql.append("UPDATE ").append(table).append(" SET ")
                            .append(String.join(", ", values));
                    if (conditions != null && !conditions.isEmpty()) sql.append(" WHERE ").append(String.join(" AND ", conditions));
                } else {
                    sql.append("Invalid UPDATE: values are missing.");
                }
            }
            case "DELETE" -> {
                sql.append("DELETE FROM ").append(table);
                if (conditions != null && !conditions.isEmpty()) sql.append(" WHERE ").append(String.join(" AND ", conditions));
            }
            default -> sql.append("Invalid query type: ").append(queryType);
        }

        return sql.toString();
    }

    private List<String> extractArray(JSONParser.ValueContext ctx) {
        if (ctx.arr() != null && ctx.arr().value() != null) {
            return ctx.arr().value().stream()
                    .map(this::visitValue)
                    .filter(Objects::nonNull)  // Ignore null values
                    .collect(Collectors.toList());
        }
        return null;
    }

    private List<String> extractValuesForInsert(JSONParser.ValueContext ctx, List<String> columns) {
        if (ctx.arr() != null && ctx.arr().value() != null && !ctx.arr().value().isEmpty()) {
            JSONParser.ValueContext firstValue = ctx.arr().value(0);
            JSONParser.ObjContext obj = firstValue.obj();
            
            if (obj != null && columns != null && !columns.isEmpty()) {
                return columns.stream()
                    .map(column -> {
                        // Find the matching column value in the object
                        for (JSONParser.PairContext pair : obj.pair()) {
                            String key = pair.STRING().getText().replace("\"", "");
                            if (column.equals(key)) {
                                String value = pair.value().getText();
                                // Add quotes for string values
                                if (value.startsWith("\"") && value.endsWith("\"")) {
                                    return "'" + value.substring(1, value.length() - 1) + "'";
                                }
                                return value;
                            }
                        }
                        return "null"; // If column not found in values
                    })
                    .collect(Collectors.toList());
            }
        }
        return null;
    }

    private List<String> extractKeyValuePairs(JSONParser.ValueContext ctx) {
        if (ctx.arr() != null && ctx.arr().value() != null) {
            return ctx.arr().value().stream()
                    .map(v -> {
                        JSONParser.ObjContext obj = v.obj();
                        if (obj != null && obj.pair(0) != null) {
                            String key = obj.pair(0).STRING().getText().replace("\"", "");
                            String value = obj.pair(0).value().getText().replace("\"", "'");
                            return key + " = " + value;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)  // Ignore null pairs
                    .collect(Collectors.toList());
        }
        return null;
    }

    private List<String> extractConditions(JSONParser.ValueContext ctx) {
        if (ctx.arr() != null && ctx.arr().value() != null) {
            return ctx.arr().value().stream()
                    .map(v -> {
                        JSONParser.ObjContext obj = v.obj();
                        if (obj != null) {
                            // Find the "column", "operator", and "value" keys 
                            String columnName = null;
                            String operator = null;
                            String value = null;
                            
                            for (int i = 0; i < obj.pair().size(); i++) {
                                String key = obj.pair(i).STRING().getText().replace("\"", "");
                                switch (key) {
                                    case "column" -> columnName = visitValue(obj.pair(i).value());
                                    case "operator" -> operator = visitValue(obj.pair(i).value());
                                    case "value" -> {
                                        value = visitValue(obj.pair(i).value());
                                        // Values need quotes for strings
                                        if (!value.startsWith("'")) value = "'" + value + "'";
                                    }
                                }
                            }
                            
                            if (columnName != null && operator != null && value != null) {
                                return columnName + " " + operator + " " + value;
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private List<String> extractOrderBy(JSONParser.ValueContext ctx) {
        if (ctx.arr() != null && ctx.arr().value() != null) {
            return ctx.arr().value().stream()
                    .map(v -> {
                        JSONParser.ObjContext obj = v.obj();
                        if (obj != null) {
                            // Find the "column" and "direction" keys
                            String columnName = null;
                            String direction = null;
                            
                            for (int i = 0; i < obj.pair().size(); i++) {
                                String key = obj.pair(i).STRING().getText().replace("\"", "");
                                if ("column".equals(key)) {
                                    columnName = visitValue(obj.pair(i).value());
                                } else if ("direction".equals(key)) {
                                    direction = visitValue(obj.pair(i).value()).toUpperCase();
                                }
                            }
                            
                            if (columnName != null && direction != null) {
                                return columnName + " " + direction;
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public String visitValue(JSONParser.ValueContext ctx) {
        if (ctx.STRING() != null) {
            return ctx.STRING().getText().replace("\"", ""); // Remove surrounding quotes
        }
        if (ctx.NUMBER() != null) {
            return ctx.NUMBER().getText();
        }
        if (ctx.obj() != null) {
            return visitObj(ctx.obj());
        }
        if (ctx.arr() != null) {
            return visitArr(ctx.arr());
        }
        if (ctx.getText().equals("true") || ctx.getText().equals("false") || ctx.getText().equals("null")) {
            return ctx.getText();
        }

        return null;
    }

    @Override
    public String visitArr(JSONParser.ArrContext ctx) {
        return ctx.value().stream()
                .map(this::visitValue)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

}
