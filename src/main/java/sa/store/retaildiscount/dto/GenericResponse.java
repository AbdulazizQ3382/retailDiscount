package sa.store.retaildiscount.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class GenericResponse<T> {

    private Boolean success;
    private LocalDateTime timestamp;
    private UUID traceId;
    private String message;
    private T data;


    public static <T> GenericResponse<T> success(T data) {
        return GenericResponse.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID())
                .message("success")
                .data(data)
                .build();
    }

    public static <T> GenericResponse<T> failure(String message) {
        return GenericResponse.<T>builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID())
                .message(message)
                .build();
    }

}
