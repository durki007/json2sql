package pl.pwr.translator_app;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.pwr.translator_app.domain.User;
import pl.pwr.translator_app.model.UserEntity;
import pl.pwr.translator_app.repository.UserRepository;
import pl.pwr.translator_app.service.UserService;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestEntityManager
class UserControllerTest {

    @LocalServerPort
    private Integer port;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        clearDatabase();
    }

    void clearDatabase() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            entityManager.getEntityManager().createNativeQuery("DELETE FROM user_entity").executeUpdate();
            entityManager.flush();
            return null;
        });
    }

    @Test
    void shouldCreateNewUser() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("""
{
  "queryType": "INSERT",
  "table": "user_entity",
  "columns": ["first_name", "last_name", "email"],
  "values": [
    {
      "first_name": "Alice",
      "last_name": "Johnson",
      "email": "alice.johnson@example.com"
    }
  ]
}
""")
                .when()
                .post("/users");

        // Print the raw response body
        response.prettyPrint();

        response.then()
                .statusCode(200)
                .body("successful", equalTo(true))
                .body("operation", equalTo("INSERT"))
                .body("rowsAffected", equalTo(1));
    }

    @Test
    void shouldSelectExistingUsers() {
        // Prepare test data in a committed transaction
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            userRepository.createTestData(3);
            return null;
        });

        // Verify data was created
        System.out.println("Size before: " + userService.findAllUsers().size());

        // Execute SELECT query and capture the response
        Response response = given()
                .contentType(ContentType.JSON)
                .body("""
{
  "queryType": "SELECT",
  "table": "user_entity",
  "columns": ["id", "first_name", "last_name", "email"]
}
""")
                .when()
                .post("/users");

        // Print the raw response body
        response.prettyPrint();

        // Validate response
        response.then()
                .statusCode(200)
                .body("successful", equalTo(true))
                .body("operation", equalTo("SELECT"))
                .body("results", hasSize(3))
                .body("results[0].firstName", startsWith("TestUser"));
    }


    @Test
    void shouldSelectSingleUserByCondition() {
        // Prepare test data in a committed transaction
        final User[] createdUser = new User[1];
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            createdUser[0] = userRepository.saveUser(User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .build());
            return null;
        });

        // Execute SELECT query with condition
        Response response = given()
                .contentType(ContentType.JSON)
                .body(String.format("""
{
  "queryType": "SELECT",
  "table": "user_entity",
  "columns": ["id", "first_name", "last_name", "email"],
  "conditions": [
    {
      "column": "id",
      "operator": "=",
      "value": %d
    }
  ]
}
""", createdUser[0].id()))
                .when()
                .post("/users");

        // Print the raw response body
        response.prettyPrint();

        response.then()
                .statusCode(200)
                .body("successful", equalTo(true))
                .body("operation", equalTo("SELECT"))
                .body("results", hasSize(1))
                .body("results[0].firstName", equalTo("John"));
    }

    // FIXME: passing multiple fields to update fails
    @Test
    void shouldUpdateExistingUser() {
        // Prepare test data in a committed transaction
        final User[] createdUser = new User[1];
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            createdUser[0] = userRepository.saveUser(User.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .build());
            return null;
        });

        // Execute UPDATE query
        Response response = given()
                .contentType(ContentType.JSON)
                .body(String.format("""
{
  "queryType": "UPDATE",
  "table": "user_entity",
  "values": [
    {
      "last_name": "Johnson"
    }
  ],
  "conditions": [
    {
      "column": "id",
      "operator": "=",
      "value": %d
    }
  ]
}
""", createdUser[0].id()))
                .when()
                .post("/users");

        // Print the raw response body
        response.prettyPrint();

        response.then()
                .statusCode(200)
                .body("successful", equalTo(true))
                .body("operation", equalTo("UPDATE"))
                .body("rowsAffected", equalTo(1));

        // Verify the update was successful in a new transaction
        transactionTemplate.execute(status -> {
            User updatedUser = userRepository.findUserById(createdUser[0].id()).orElseThrow();
            assertThat(updatedUser.firstName()).isEqualTo("Jane");
            assertThat(updatedUser.lastName()).isEqualTo("Johnson");
            return null;
        });
    }

    @Test
    void shouldDeleteExistingUser() {
        // Prepare test data in a committed transaction
        final User[] createdUser = new User[1];
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            createdUser[0] = userRepository.saveUser(User.builder()
                    .firstName("Bob")
                    .lastName("Taylor")
                    .email("bob.taylor@example.com")
                    .build());
            return null;
        });

        // Verify the user exists
        transactionTemplate.execute(status -> {
            assertThat(userRepository.findUserById(createdUser[0].id())).isPresent();
            return null;
        });

        // Execute DELETE query
        Response response = given()
                .contentType(ContentType.JSON)
                .body("""
{
  "queryType": "DELETE",
  "table": "user_entity",
  "conditions": [
    {
      "column": "last_name",
      "operator": "LIKE",
      "value": "Taylor"
    }
  ]
}
""")
                .when()
                .post("/users");

        // Print the raw response body
        response.prettyPrint();

        response.then()
                .statusCode(200)
                .body("successful", equalTo(true))
                .body("operation", equalTo("DELETE"))
                .body("rowsAffected", equalTo(1));

        // Verify the user was deleted in a new transaction
        transactionTemplate.execute(status -> {
            assertThat(userRepository.findUserById(createdUser[0].id())).isEmpty();
            return null;
        });
    }

    @Test
    void shouldDeleteAllUsersWithoutCondition() {
        // Prepare test data in a committed transaction
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            userRepository.createTestData(5);
            return null;
        });

        // Verify data was created
        transactionTemplate.execute(status -> {
            assertThat(userRepository.findAllUsers()).hasSize(5);
            return null;
        });

        // Execute DELETE query without conditions
        Response response = given()
                .contentType(ContentType.JSON)
                .body("""
{
  "queryType": "DELETE",
  "table": "user_entity"
}
""")
                .when()
                .post("/users");

        // Print the raw response body
        response.prettyPrint();

        response.then()
                .statusCode(200)
                .body("successful", equalTo(true))
                .body("operation", equalTo("DELETE"))
                .body("rowsAffected", equalTo(5));

        // Verify all users were deleted in a new transaction
        transactionTemplate.execute(status -> {
            assertThat(userRepository.findAllUsers()).isEmpty();
            return null;
        });
    }

    @Test
    void shouldDeleteAllUsersWithCondition() {
        // Prepare test data in a committed transaction
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            userRepository.createTestData(5);
            return null;
        });

        // Plant user to delete in test
        transactionTemplate.execute( status -> {
            List<UserEntity> plantedUserEntities = List.of(UserEntity.builder()
                    .firstName("Planted")
                    .lastName("User")
                    .email("email@spam.com").build());
            userRepository.plantTestData(plantedUserEntities);
            return null;
        });

        // Verify data was created
        transactionTemplate.execute(status -> {
            assertThat(userRepository.findAllUsers()).hasSize(5 + 1);
            return null;
        });

        // Execute DELETE query with conditions
        Response response = given()
                .contentType(ContentType.JSON)
                .body("""
{
  "queryType": "DELETE",
  "table": "user_entity",
  "conditions": [
    { "column": "email", "operator": "LIKE", "value": "%spam.com" }
  ]
}
""")
                .when()
                .post("/users");

        // Print the raw response body
        response.prettyPrint();

        response.then()
                .statusCode(200)
                .body("successful", equalTo(true))
                .body("operation", equalTo("DELETE"))
                .body("rowsAffected", equalTo(1));

        // Verify only one user was deleted in a new transaction
        transactionTemplate.execute(status -> {
            assertThat(userRepository.findAllUsers()).hasSize(5);
            return null;
        });
    }
}
