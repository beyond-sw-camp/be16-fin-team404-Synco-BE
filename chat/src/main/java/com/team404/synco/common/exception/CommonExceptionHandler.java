package com.team404.synco.common.exception;

import com.team404.synco.common.dto.ResponseDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * ✅ 1. 존재하지 않는 채팅방, 유저 등
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseDto<?>> handleEntityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseDto.fail(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    /**
     * ✅ 2. 잘못된 요청 파라미터 또는 유효성 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<?>> handleValidation(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "요청 데이터가 유효하지 않습니다.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDto.fail(HttpStatus.BAD_REQUEST, errorMsg));
    }

    /**
     * ✅ 3. 잘못된 비즈니스 로직 (예: 본인 아닌데 채팅방에 접근 시도 등)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<?>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    /**
     * ✅ 4. 예기치 못한 서버 내부 오류
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handleGeneral(Exception e) {
        e.printStackTrace(); // 디버깅 시 스택 확인용 (운영에서는 log.error로 교체)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }

    /**
     * ✅ 5. 인증 실패 (예: 토큰 누락, 권한 없음 등)
     */
    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ResponseDto<?>> handleAuthentication(AuthenticationServiceException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDto.fail(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }
}
