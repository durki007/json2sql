package pl.pwr.translator_app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.pwr.parser.QueryTranslator;
import pl.pwr.translator_app.domain.User;
import pl.pwr.translator_app.dto.QueryRequestDTO;
import pl.pwr.translator_app.repository.UserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<User> queryUsers(QueryRequestDTO query) {
        try {
            // Convert DTO back to the JSON string
            String jsonQuery = objectMapper.writeValueAsString(query);
            log.info("JSON Query: {}", jsonQuery);

            // Parse JSON using ANTLR-based translator
            QueryTranslator queryTranslator = new QueryTranslator();

            String translatedQuery = queryTranslator.translate(jsonQuery);
            log.info("Translated Query: {}", translatedQuery);

            // Execute the translated SQL query
            return userRepository.queryUsers(translatedQuery);
        } catch (JsonProcessingException e) {
            log.error("Error executing query: {}", query, e);
            throw new RuntimeException("Error converting DTO to JSON", e);
        }
    }
}
