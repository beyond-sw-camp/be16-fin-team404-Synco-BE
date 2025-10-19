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
        Long memberSeq = null;

        for (Object arg : pjp.getArgs()) {
            if (arg instanceof Long) {
                memberSeq = (Long) arg;
                break;
            }
        }

        try {
            Object result = pjp.proceed();

            // ✅ 1️⃣ Redis 결과가 null이거나 빈 리스트인 경우 fallback 실행
            if (result == null ||
                    (result instanceof List<?> list && list.isEmpty())) {
                log.warn("⚠️ Redis 결과 없음 or 비어 있음 → TaskFeign Fallback 실행 [{}]", method);
                return taskFeign.findMyWorkSpaceList(memberSeq);
            }

            // ✅ 2️⃣ DTO 내부 리스트 검사 (MyWorkSpaceListResDto 형태일 경우)
            if (result instanceof MyWorkSpaceListResDto dto &&
                    (dto.getWorkSpaceInfoDtoList() == null || dto.getWorkSpaceInfoDtoList().isEmpty())) {
                log.warn("⚠️ Redis DTO 내부 비어 있음 → TaskFeign Fallback 실행 [{}]", method);
                return taskFeign.findMyWorkSpaceList(memberSeq);
            }

            // ✅ 정상 결과면 그대로 반환
            return result;

        } catch (DataAccessException e) {
            log.warn("⚠️ Redis 접근 실패 (DataAccessException) → TaskFeign Fallback 실행 [{}]", method);
            return taskFeign.findMyWorkSpaceList(memberSeq);
        } catch (Exception e) {
            log.warn("⚠️ Redis 조회 예외 발생 → TaskFeign Fallback 실행 [{}]: {}", method, e.getMessage());
            return taskFeign.findMyWorkSpaceList(memberSeq);
        }
    }
}
