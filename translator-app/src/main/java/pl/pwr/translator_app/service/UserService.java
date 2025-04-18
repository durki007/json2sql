package pl.pwr.translator_app.service;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.pwr.parser.QueryTranslator;
import pl.pwr.translator_app.dto.QueryRequestDTO;
import pl.pwr.translator_app.dto.QueryResultDTO;
import pl.pwr.translator_app.repository.UserRepository;
import pl.pwr.translator_app.result.QueryResult;

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
            QueryResult queryResult = userRepository.queryUsers(translatedQuery);
            
            // Create appropriate response based on query type
            QueryResultDTO result = new QueryResultDTO();
            result.setSuccessful(true);
            result.setQuery(translatedQuery);
            result.setRowsAffected(queryResult.getRowsAffected());

            switch (queryType) {
                case "SELECT" -> {
                    result.setOperation("SELECT");
                    result.setMessage("Query executed successfully");
                    result.setResults(queryResult.getUsers());
                }
                case "INSERT" -> {
                    result.setOperation("INSERT");
                    result.setMessage("Data inserted successfully");
                    result.setResults(Collections.emptyList());
                }
                case "UPDATE" -> {
                    result.setOperation("UPDATE");
                    result.setMessage("Data updated successfully");
                    result.setResults(Collections.emptyList());
                }
                case "DELETE" -> {
                    result.setOperation("DELETE");
                    result.setMessage("Data deleted successfully");
                    result.setResults(Collections.emptyList());
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
