package pl.pwr.translator_app.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.pwr.parser.QueryTranslator;
import pl.pwr.translator_app.domain.User;
import pl.pwr.translator_app.dto.QueryRequestDTO;
import pl.pwr.translator_app.dto.QueryResultDTO;
import pl.pwr.translator_app.model.UserEntity;
import pl.pwr.translator_app.repository.UserRepository;
import pl.pwr.translator_app.result.QueryResult;

@Service
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
    
    // CRUD operations
    
    /**
     * Create a new user
     * 
     * @param user the user to create
     * @return the created user
     */
    public User createUser(User user) {
        return userRepository.saveUser(user);
    }
    
    /**
     * Find a user by ID
     * 
     * @param id the user ID
     * @return an Optional containing the user if found
     */
    public Optional<User> findUserById(Long id) {
        return userRepository.findUserById(id);
    }
    
    /**
     * Get all users
     * 
     * @return a list of all users
     */
    public List<User> findAllUsers() {
        return userRepository.findAllUsers();
    }
    
    /**
     * Update a user
     * 
     * @param user the user to update
     * @return the updated user
     */
    public User updateUser(User user) {
        return userRepository.saveUser(user);
    }
    
    /**
     * Delete a user by ID
     * 
     * @param id the ID of the user to delete
     * @return true if the user was deleted, false if not found
     */
    public boolean deleteUser(Long id) {
        return userRepository.deleteById(id);
    }
    
    /**
     * Delete all users
     */
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
    
    /**
     * Create sample test data for integration tests
     * 
     * @param count number of users to create
     * @return list of created users
     */
    public List<User> createTestData(int count) {
        return userRepository.createTestData(count);
    }
}
