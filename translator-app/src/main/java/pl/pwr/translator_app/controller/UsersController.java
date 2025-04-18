package pl.pwr.translator_app.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import pl.pwr.translator_app.dto.QueryRequestDTO;
import pl.pwr.translator_app.dto.QueryResultDTO;
import pl.pwr.translator_app.service.UserService;

@RestController
@AllArgsConstructor
public class UsersController {
    private final UserService userService;

    @PostMapping("/users")
    public QueryResultDTO executeQuery(@RequestBody(required = true) QueryRequestDTO queryRequest) {
        return userService.queryUsers(queryRequest);
    }
}
