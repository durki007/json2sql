package pl.pwr.translator_app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.pwr.parser.QueryTranslator;
import pl.pwr.translator_app.domain.User;
import pl.pwr.translator_app.dto.QueryRequestDTO;
import pl.pwr.translator_app.dto.QueryResultDTO;
import pl.pwr.translator_app.repository.UserRepository;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QueryResultDTO queryUsers(QueryRequestDTO query) {
        try {
            // Convert DTO back to the JSON string
            String jsonQuery = objectMapper.writeValueAsString(query);
            log.info("JSON Query: {}", jsonQuery);

            // Parse JSON using ANTLR-based translator
            QueryTranslator queryTranslator = new QueryTranslator();

            String translatedQuery = queryTranslator.translate(jsonQuery);
            log.info("Translated Query: {}", translatedQuery);

            String queryType = query.getQueryType().toUpperCase();
            
            // Execute the translated SQL query
            List<User> users = userRepository.queryUsers(translatedQuery);
            
            // Create appropriate response based on query type
            QueryResultDTO result = new QueryResultDTO();
            result.setSuccessful(true);
            result.setQuery(translatedQuery);

            switch (queryType) {
                case "SELECT" -> {
                    result.setOperation("SELECT");
                    result.setMessage("Query executed successfully");
                    result.setResults(users);
                    result.setRowsAffected(users.size());
                }
                case "INSERT" -> {
                    result.setOperation("INSERT");
                    result.setMessage("Data inserted successfully");
                    result.setResults(Collections.emptyList());
                    result.setRowsAffected(1); // Assuming one row inserted
                }
                case "UPDATE" -> {
                    result.setOperation("UPDATE");
                    result.setMessage("Data updated successfully");
                    result.setResults(Collections.emptyList());
                    // We don't know exactly how many rows affected, but at least the query succeeded
                    result.setRowsAffected(0);
                }
                case "DELETE" -> {
                    result.setOperation("DELETE");
                    result.setMessage("Data deleted successfully");
                    result.setResults(Collections.emptyList());
                    // We don't know exactly how many rows affected, but at least the query succeeded
                    result.setRowsAffected(0);
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error executing query: {}", query, e);
            QueryResultDTO result = new QueryResultDTO();
            result.setSuccessful(false);
            result.setMessage("Error: " + e.getMessage());
            result.setResults(Collections.emptyList());
            return result;
        }
    }
}
