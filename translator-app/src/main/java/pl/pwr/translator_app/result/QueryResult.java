package pl.pwr.translator_app.result;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.pwr.translator_app.domain.User;

@Data
@AllArgsConstructor
public class QueryResult {
    private List<User> users;
    private int rowsAffected;
} 