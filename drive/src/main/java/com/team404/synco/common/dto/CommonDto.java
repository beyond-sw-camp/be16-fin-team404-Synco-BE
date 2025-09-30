package com.team404.synco.common.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Data
@Builder
public class CommonDto<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T data;

    public static <T> CommonDto<T> ok(T data, HttpStatus status) {
        return CommonDto.<T>builder()
                .success(true)
                .code(status.value())
                .data(data)
                .message("요청이 성공적으로 처리되었습니다.")
                .build();
    }

    public static <T> CommonDto<T> fail(HttpStatus status, String message) {
        return CommonDto.<T>builder()
                .success(false)
                .code(status.value())
                .message(message)
                .data(null)
                .build();
    }
}
