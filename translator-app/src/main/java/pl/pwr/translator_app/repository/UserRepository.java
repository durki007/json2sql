package pl.pwr.translator_app.repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.pwr.translator_app.domain.User;
import pl.pwr.translator_app.mapper.UserMapper;
import pl.pwr.translator_app.model.UserEntity;
import pl.pwr.translator_app.result.QueryResult;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository {
    private final UserMapper userMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Transactional
    public QueryResult queryUsers(String query) {
        try {
            // Check if this is a SELECT query
            if (query.trim().toUpperCase().startsWith("SELECT")) {
                Query nativeQuery = entityManager.createNativeQuery(query, UserEntity.class);
                List<UserEntity> entities = (List<UserEntity>) nativeQuery.getResultList();

                List<User> users = entities.stream()
                        .map(userMapper::map)
                        .collect(Collectors.toList());
                
                return new QueryResult(users, users.size());
            } else {
                // Handle non-SELECT queries (INSERT, UPDATE, DELETE)
                Query nativeQuery = entityManager.createNativeQuery(query);
                int rowsAffected = nativeQuery.executeUpdate();
                log.info("Non-SELECT query executed successfully. Rows affected: {}", rowsAffected);
                return new QueryResult(Collections.emptyList(), rowsAffected);
            }
        } catch (Exception e) {
            log.error("Error executing dynamic query: {}", query, e);
            return new QueryResult(Collections.emptyList(), 0);
        }
    }
    
    // CRUD operations
    
    /**
     * Save a user entity to the database
     * 
     * @param userEntity the entity to save
     * @return the saved entity
     */
    @Transactional
    public UserEntity save(UserEntity userEntity) {
        if (userEntity.getId() == 0) {
            entityManager.persist(userEntity);
            return userEntity;
        } else {
            return entityManager.merge(userEntity);
        }
    }
    
    /**
     * Save a domain user object to the database
     * 
     * @param user the user to save
     * @return the saved user
     */
    @Transactional
    public User saveUser(User user) {
        UserEntity entity = userMapper.map(user);
        entity = save(entity);
        return userMapper.map(entity);
    }
    
    /**
     * Find a user entity by ID
     * 
     * @param id the user ID
     * @return an Optional containing the user if found
     */
    public Optional<UserEntity> findById(Long id) {
        return Optional.ofNullable(entityManager.find(UserEntity.class, id));
    }
    
    /**
     * Find a domain user by ID
     * 
     * @param id the user ID
     * @return an Optional containing the user if found
     */
    public Optional<User> findUserById(Long id) {
        return findById(id).map(userMapper::map);
    }
    
    /**
     * Find all user entities
     * 
     * @return a list of all user entities
     */
    public List<UserEntity> findAll() {
        TypedQuery<UserEntity> query = entityManager.createQuery(
                "SELECT u FROM UserEntity u", UserEntity.class);
        return query.getResultList();
    }
    
    /**
     * Find all domain users
     * 
     * @return a list of all users
     */
    public List<User> findAllUsers() {
        return findAll().stream()
                .map(userMapper::map)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete a user entity
     * 
     * @param userEntity the entity to delete
     */
    @Transactional
    public void delete(UserEntity userEntity) {
        if (entityManager.contains(userEntity)) {
            entityManager.remove(userEntity);
        } else {
            entityManager.remove(entityManager.merge(userEntity));
        }
    }
    
    /**
     * Delete a user by ID
     * 
     * @param id the ID of the user to delete
     * @return true if the user was deleted, false if not found
     */
    @Transactional
    public boolean deleteById(Long id) {
        Optional<UserEntity> entity = findById(id);
        if (entity.isPresent()) {
            delete(entity.get());
            return true;
        }
        return false;
    }
    
    /**
     * Delete all users from the database
     */
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM UserEntity").executeUpdate();
    }
    
    /**
     * Create sample test data for integration tests
     * 
     * @param count number of users to create
     * @return list of created users
     */
    @Transactional
    public List<User> createTestData(int count) {
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            UserEntity entity = UserEntity.builder()
                    .firstName("TestUser" + i)
                    .lastName("LastName" + i)
                    .email("test" + i + "@example.com")
                    .build();
            
            entityManager.persist(entity);
            users.add(userMapper.map(entity));
        }
        
        return users;
    }
}