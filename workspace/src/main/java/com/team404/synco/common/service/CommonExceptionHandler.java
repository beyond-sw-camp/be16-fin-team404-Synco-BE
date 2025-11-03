package com.team404.synco.common.service;

import com.team404.synco.common.dto.ResponseDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        e.printStackTrace();
        return ResponseEntity.badRequest()
                .body(ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseDto<?>> handleIllegalStateException(IllegalStateException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseDto<?>> handleEntityNotFoundException(EntityNotFoundException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseDto.fail(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(SerializationException.class)
    public ResponseEntity<?> handleSerializationException(SerializationException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseDto.fail(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<?>> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "입력값이 올바르지 않습니다.";
        e.printStackTrace();
        return ResponseEntity.badRequest()
                .body(ResponseDto.fail(HttpStatus.BAD_REQUEST, errorMessage));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDto<?>> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e, HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        e.printStackTrace();
        // SSE(text/event-stream) 요청일 경우 ResponseDto 반환하지 않음
        if (acceptHeader != null && acceptHeader.contains("text/event-stream")) {
            return ResponseEntity.ok().build(); // 아무 내용도 보내지 않음 (스트림 유지 or 종료)
        }

        // 일반 요청일 경우 기존 ResponseDto 그대로 반환
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR,
                        "서버 오류가 발생했습니다: " + e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ResponseDto<?>> handleSecurityException(SecurityException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDto.fail(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDto<?>> handleAccessDeniedException(AccessDeniedException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseDto.fail(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ResponseDto<?>> handleMultipartException(MultipartException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
}
