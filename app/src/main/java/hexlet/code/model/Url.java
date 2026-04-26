package hexlet.code.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Url {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Integer lastStatusCode;
    private LocalDateTime lastCheckAt;

    public Url(String name) {
        this.name = name;
    }
}
