package com.team404.synco.common.exception;

import com.team404.synco.common.dto.CommonDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ======================== Entity/리소스 관련 예외 ========================
     */

    // JPA Entity 조회 실패
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException e) {
        log.error("[EntityNotFoundException] {}", e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // Optional.get() 에서 값 없음
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElement(NoSuchElementException e) {
        log.error("[NoSuchElementException] {}", e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // JPA 결과가 없음
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<?> handleEmptyResultDataAccess(EmptyResultDataAccessException e) {
        log.error("[EmptyResultDataAccessException] {}", e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * ======================== DB 관련 예외 ========================
     */

    // DB 무결성 제약 조건 위반 (UniqueKey, FK 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException e) {
        log.error("[DataIntegrityViolationException] {}", e.getMessage());
        return buildError(HttpStatus.CONFLICT, e.getMessage());
    }

    // 트랜잭션 시스템 에러
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<?> handleTransactionSystem(TransactionSystemException e) {
        log.error("[TransactionSystemException] {}", e.getMessage(), e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션 처리 중 오류가 발생했습니다.");
    }

    // JDBC 에러 (SQLSyntaxError, Connection 에러 등 포함)
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> handleSQLException(SQLException e) {
        log.error("[SQLException] {}", e.getMessage(), e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 처리 중 오류가 발생했습니다.");
    }

    /**
     * ======================== 런타임 예외 ========================
     */

    // 잘못된 인자 전달
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] {}", e.getMessage(), e);
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 잘못된 상태에서 로직 호출
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        log.error("[IllegalStateException] {}", e.getMessage(), e);
        return buildError(HttpStatus.CONFLICT, e.getMessage());
    }

    // Null 참조
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointer(NullPointerException e) {
        log.error("[NullPointerException]", e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 Null 참조 오류가 발생했습니다.");
    }

    // RuntimeException (일반적인 런타임 예외)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        log.error("[RuntimeException] {}", e.getMessage(), e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "서버 처리 중 오류가 발생했습니다.");
    }

    /**
     * ======================== 요청/검증 관련 예외 ========================
     */

    // DTO @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "요청 데이터가 유효하지 않습니다.";
        log.error("[Validation 실패] {}", errorMessage);
        return buildError(HttpStatus.BAD_REQUEST, errorMessage);
    }

    // Binding 실패 (단순 필드 매핑 에러 포함)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("[BindException] {}", errorMessage);
        return buildError(HttpStatus.BAD_REQUEST, errorMessage);
    }

    // @Validated 메서드 파라미터 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException e) {
        log.error("[ConstraintViolationException] {}", e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다.");
    }

    // 필수 요청 파라미터(@RequestParam) 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        log.error("[MissingServletRequestParameterException] {}", e.getParameterName());
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // PathVariable, RequestParam 타입 불일치 (int에 문자열 등)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String errorMessage = String.format("요청 파라미터 '%s'의 타입이 '%s'이어야 합니다.",
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "알 수 없음");
        log.error("[MethodArgumentTypeMismatchException] {}", errorMessage);
        return buildError(HttpStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * ======================== HTTP 요청 처리 예외 ========================
     */

    // JSON 파싱 에러
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("[HttpMessageNotReadableException] {}", e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "JSON 요청을 읽을 수 없습니다.");
    }

    /**
     * ======================== 파일 업로드 관련 예외 ========================
     */

    // 파일 업로드 크기 초과
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.error("[MaxUploadSizeExceededException] {}", e.getMessage());
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 파일 크기가 제한을 초과했습니다. 파일 크기를 확인해주세요.");
    }

    // Multipart 요청 처리 중 오류
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMultipartException(MultipartException e) {
        log.error("[MultipartException] {}", e.getMessage(), e);
        return buildError(HttpStatus.BAD_REQUEST, "파일 업로드 처리 중 오류가 발생했습니다. 파일을 다시 선택해주세요.");
    }

    // 요청 body나 multipart 데이터 누락 (파일 관련)
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> handleMissingServletRequestPart(MissingServletRequestPartException e) {
        log.error("[MissingServletRequestPartException] {}", e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "업로드할 파일이 누락되었습니다. 파일을 선택해주세요.");
    }

    /**
     * ======================== IO 관련 예외 ========================
     */

    // IO 에러 (파일 읽기/쓰기 실패)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException e) {
        log.error("[IOException] {}", e.getMessage(), e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다.");
    }

    /**
     * ======================== Fallback (그 외 모든 예외) ========================
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        log.error("[Unhandled Exception] {}", e.getMessage(), e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
    }

    /**
     * ======================== 공통 ResponseEntity 생성 ========================
     */
    private ResponseEntity<?> buildError(HttpStatus status, String message) {
        return new ResponseEntity<>(CommonDto.fail(status,message), status);
    }
}
