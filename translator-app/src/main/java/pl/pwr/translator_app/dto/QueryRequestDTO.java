package pl.pwr.translator_app.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryRequestDTO {
    private String queryType;  // SELECT, INSERT, UPDATE, DELETE
    private String table;
    private List<String> columns;
    private List<Map<String, String>> values;
    private List<Condition> conditions;
    private List<OrderBy> orderBy;
    private List<String> groupBy;
    private Integer limit;

    @Data
    public static class Condition {
        private String column;
        private String operator;  // =, !=, >, <, LIKE, IN
        private String value;
    }

    @Data
    public static class OrderBy {
        private String column;
        private String direction;  // ASC, DESC
    }
}
