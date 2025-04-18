package pl.pwr.translator_app.dto;

import java.util.List;

import lombok.Data;
import pl.pwr.translator_app.domain.User;

@Data
public class QueryResultDTO {
    private boolean successful;
    private String operation;
    private String message;
    private String query;
    private int rowsAffected;
    private List<User> results;
} 