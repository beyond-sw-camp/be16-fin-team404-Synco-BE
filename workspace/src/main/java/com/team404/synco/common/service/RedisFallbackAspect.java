package com.team404.synco.common.service;

import com.team404.synco.workspace.dto.MyWorkSpaceListResDto;
import com.team404.synco.workspace.service.TaskFeign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisFallbackAspect {

    private final TaskFeign taskFeign;

    @Around("@annotation(RedisFallback)")
    public Object handleRedisFallback(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().getName();
        Long identifier = null; // memberSeq 또는 workSpaceSeq

        // 인자 중 Long 타입 추출
        for (Object arg : pjp.getArgs()) {
            if (arg instanceof Long l) {
                identifier = l;
                break;
            }
        }

        try {
            Object result = pjp.proceed();

            // Redis 결과가 null이거나 비어 있으면 fallback
            if (result == null || (result instanceof List<?> list && list.isEmpty())) {
                log.warn("Redis 결과 없음 → Fallback 실행 [{}]", method);
                return executeFallback(method, identifier);
            }

            // MyWorkSpaceListResDto 형태일 때 내부 리스트 검사
            if (result instanceof MyWorkSpaceListResDto dto &&
                    (dto.getWorkSpaceInfoResDtoList() == null || dto.getWorkSpaceInfoResDtoList().isEmpty())) {
                log.warn("Redis DTO 내부 비어 있음 → Fallback 실행 [{}]", method);
                return executeFallback(method, identifier);
            }

            // 정상 결과 반환
            return result;

        } catch (DataAccessException e) {
            log.warn("Redis 접근 실패 → Fallback 실행 [{}]: {}", method, e.getMessage());
            return executeFallback(method, identifier);
        } catch (Exception e) {
            log.warn("Redis 조회 예외 → Fallback 실행 [{}]: {}", method, e.getMessage());
            return executeFallback(method, identifier);
        }
    }

    /**
     * Redis Fallback 실행 분기
     */
    private Object executeFallback(String method, Long id) {
        if (id == null) {
            log.error("Fallback 실행 실패: ID(Long)가 존재하지 않습니다. [{}]", method);
            return Collections.emptyList();
        }

        // 메서드명 기준으로 적절한 Feign 호출 분기
        if (method.contains("findMyWorkSpaceList")) {
            log.info("TaskFeign.findMyWorkSpaceList() Fallback 호출 (memberSeq={})", id);
            return taskFeign.findMyWorkSpaceList(id);
        }

        if (method.contains("findWorkSpaceMemberList")) {
            log.info("TaskFeign.findWorkSpaceMemberList() Fallback 호출 (workSpaceSeq={})", id);
            return taskFeign.findWorkSpaceMemberList(id);
        }
        log.error("Fallback 실행 실패: aop 대상 메서드가 아닙니다.. [{}]", method);
        return Collections.emptyList();
    }
}
