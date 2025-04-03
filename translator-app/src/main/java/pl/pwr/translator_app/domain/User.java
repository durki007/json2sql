package pl.pwr.translator_app.domain;

import lombok.Builder;

@Builder(toBuilder = true)
public record User(
        long id,
        String firstName,
        String lastName,
        String email
) {
}
