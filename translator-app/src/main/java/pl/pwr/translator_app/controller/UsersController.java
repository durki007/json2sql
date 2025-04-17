package pl.pwr.translator_app.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.pwr.translator_app.domain.User;
import pl.pwr.translator_app.service.UserService;
import pl.pwr.translator_app.dto.QueryRequestDTO;

import java.util.List;

@RestController
@AllArgsConstructor
public class UsersController {
    private final UserService userService;

    @PostMapping("/users")
    public List<User> getUsers(@RequestBody(required = true) QueryRequestDTO queryRequest) {
        return userService.queryUsers(queryRequest);
    }
}
