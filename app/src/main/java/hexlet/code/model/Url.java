package hexlet.code.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Url {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
