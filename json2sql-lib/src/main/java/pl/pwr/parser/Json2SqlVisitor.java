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
                case "values" -> values = value != null ? extractKeyValuePairs(pair.value()) : null;
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
                if (limit != null && !limit.isEmpty()) sql.append(" LIMIT ").append(limit);
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
                        if (obj != null && obj.pair(0) != null && obj.pair(1) != null && obj.pair(2) != null) {
                            String column = obj.pair(0).STRING().getText().replace("\"", "");
                            String operator = obj.pair(1).value().getText().replace("\"", "");
                            String value = obj.pair(2).value().getText().replace("\"", "'");
                            return column + " " + operator + " " + value;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)  // Ignore null conditions
                    .collect(Collectors.toList());
        }
        return null;
    }

    private List<String> extractOrderBy(JSONParser.ValueContext ctx) {
        if (ctx.arr() != null && ctx.arr().value() != null) {
            return ctx.arr().value().stream()
                    .map(v -> {
                        JSONParser.ObjContext obj = v.obj();
                        if (obj != null && obj.pair(0) != null && obj.pair(1) != null) {
                            String column = obj.pair(0).STRING().getText().replace("\"", "");
                            String direction = obj.pair(1).value().getText().replace("\"", "").toUpperCase();
                            return column + " " + direction;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)  // Ignore null orders
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
