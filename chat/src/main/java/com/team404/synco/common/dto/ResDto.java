package com.team404.synco.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ResDto<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T data;

    public static <T> ResDto<T> ok(T data, HttpStatus status) {
        return ResDto.<T>builder()
                .success(true)
                .code(status.value())
                .data(data)
                .message("요청이 성공적으로 처리되었습니다.")
                .build();
    }

    public static <T> ResDto<T> fail(HttpStatus status, String message) {
        return ResDto.<T>builder()
                .success(false)
                .code(status.value())
                .message(message)
                .data(null)
                .build();
    }
}
