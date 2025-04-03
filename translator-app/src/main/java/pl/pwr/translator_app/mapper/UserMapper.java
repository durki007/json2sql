package pl.pwr.translator_app.mapper;

import org.springframework.stereotype.Component;
import pl.pwr.translator_app.domain.User;
import pl.pwr.translator_app.model.UserEntity;

@Component
public class UserMapper {

    public UserEntity map(User user) {
        if (user == null) return UserEntity.builder().build();

        return UserEntity.builder()
                .id(user.id())
                .firstName(user.firstName())
                .lastName(user.lastName())
                .email(user.email())
                .build();
    }

    public User map(UserEntity userEntity) {
        if (userEntity == null) return User.builder().build();

        return User.builder()
                .id(userEntity.getId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .email(userEntity.getEmail())
                .build();
    }
}
