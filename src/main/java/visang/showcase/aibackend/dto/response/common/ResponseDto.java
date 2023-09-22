package visang.showcase.aibackend.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private final ResponseStatus status;
    private String message;
    private final T data;
}
