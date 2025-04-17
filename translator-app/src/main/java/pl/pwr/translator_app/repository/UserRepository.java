package pl.pwr.translator_app.repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.pwr.translator_app.domain.User;
import pl.pwr.translator_app.mapper.UserMapper;
import pl.pwr.translator_app.model.UserEntity;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository {
    private final UserMapper userMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Transactional
    public List<User> queryUsers(String query) {
        try {
            // Check if this is a SELECT query
            if (query.trim().toUpperCase().startsWith("SELECT")) {
                Query nativeQuery = entityManager.createNativeQuery(query, UserEntity.class);
                List<UserEntity> entities = (List<UserEntity>) nativeQuery.getResultList();

                return entities.stream()
                        .map(userMapper::map)
                        .collect(Collectors.toList());
            } else {
                // Handle non-SELECT queries (INSERT, UPDATE, DELETE)
                Query nativeQuery = entityManager.createNativeQuery(query);
                int rowsAffected = nativeQuery.executeUpdate();
                log.info("Non-SELECT query executed successfully. Rows affected: {}", rowsAffected);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error executing dynamic query: {}", query, e);
            return Collections.emptyList();
        }
    }
}