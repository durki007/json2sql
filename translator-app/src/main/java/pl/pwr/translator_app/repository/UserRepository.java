package pl.pwr.translator_app.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pl.pwr.translator_app.domain.User;
import pl.pwr.translator_app.mapper.UserMapper;
import pl.pwr.translator_app.model.UserEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository {
    private final UserMapper userMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<User> queryUsers(String query) {
        try {
            Query nativeQuery = entityManager.createNativeQuery(query, UserEntity.class);
            List<UserEntity> entities = (List<UserEntity>) nativeQuery.getResultList();

            return entities.stream()
                    .map(userMapper::map)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error executing dynamic query: {}", query, e);
            return Collections.emptyList();
        }
    }
}