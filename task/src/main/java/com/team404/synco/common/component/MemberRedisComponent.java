package com.team404.synco.common.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.virtualmeeting.dto.MemberInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class MemberRedisComponent {
    private final RedisTemplate<String, Object> memberRedisTemplate;
    private final RedisTemplate<String, Object> workSpaceRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MEMBER_KEY_PREFIX = "memberSeq:";
    private static final String WORKSPACE_KEY_PREFIX = "workSpaceSeq:";
    private static final String MEMBER_NAME = "memberName";
    private static final String MEMBER_PROFILE_URL = "memberProfileUrl";
    private static final String ACTIVE_STATUS = "activeStatus";
    private static final String MEMBER_LIST = "memberList";

    public MemberRedisComponent(@Qualifier("memberInventory") RedisTemplate<String, Object> memberRedisTemplate,
                               @Qualifier("workSpaceInventory") RedisTemplate<String, Object> workSpaceRedisTemplate) {
        this.memberRedisTemplate = memberRedisTemplate;
        this.workSpaceRedisTemplate = workSpaceRedisTemplate;
    }

    public String getMemberName(final long memberSeq) {
        return Objects.requireNonNull(memberRedisTemplate.opsForHash().get(MEMBER_KEY_PREFIX + memberSeq, "memberName")).toString();
    }

    public String getMemberProfileUrl(final long memberSeq) {
        return Objects.requireNonNull(memberRedisTemplate.opsForHash().get(MEMBER_KEY_PREFIX + memberSeq, MEMBER_PROFILE_URL)).toString();
    }

    /**
     * 단일 회원 정보 조회
     */
    public MemberInfoDto getMemberInfo(Long memberSeq) {
        try {
            String memberKey = MEMBER_KEY_PREFIX + memberSeq;
            Map<Object, Object> info = memberRedisTemplate.opsForHash().entries(memberKey);
            
            if (info.isEmpty()) {
                log.warn("Redis에서 회원 정보를 찾을 수 없습니다. (memberSeq={})", memberSeq);
                return null;
            }

            return MemberInfoDto.builder()
                    .memberSeq(memberSeq)
                    .name((String) info.get(MEMBER_NAME))
                    .profileImageUrl((String) info.get(MEMBER_PROFILE_URL))
                    .activeStatus(ActiveStatus.valueOf((String) info.get(ACTIVE_STATUS)))
                    .build();

        } catch (DataAccessException e) {
            log.warn("Redis 접근 실패 (memberSeq={})", memberSeq);
            throw e;
        } catch (Exception e) {
            log.warn("Redis 조회 예외 (memberSeq={}): {}", memberSeq, e.getMessage());
            return null;
        }
    }

    /**
     * 여러 회원 정보 조회
     */
    public List<MemberInfoDto> getMemberInfoList(List<Long> memberSeqList) {
        if (memberSeqList == null || memberSeqList.isEmpty()) {
            return Collections.emptyList();
        }

        return memberSeqList.stream()
                .map(this::getMemberInfo)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 워크스페이스 멤버 목록 조회
     */
    public List<MemberInfoDto> getWorkSpaceMemberList(Long workSpaceSeq) {
        try {
            String workSpaceKey = WORKSPACE_KEY_PREFIX + workSpaceSeq;
            Object cachedValue = workSpaceRedisTemplate.opsForHash().get(workSpaceKey, MEMBER_LIST);
            if (cachedValue == null) {
                log.warn("Redis에서 워크스페이스 멤버 목록을 찾을 수 없습니다. (workSpaceSeq={})", workSpaceSeq);
                return Collections.emptyList();
            }

            List<Long> memberList = objectMapper.readValue(cachedValue.toString(), new TypeReference<>() {});
            if (memberList.isEmpty()) {
                return Collections.emptyList();
            }

            return memberList.stream()
                    .map(seq -> {
                        String memberKey = MEMBER_KEY_PREFIX + seq;
                        Map<Object, Object> info = memberRedisTemplate.opsForHash().entries(memberKey);
                        if (info.isEmpty()) return null;

                        return MemberInfoDto.builder()
                                .memberSeq(seq)
                                .name((String) info.get(MEMBER_NAME))
                                .profileImageUrl((String) info.get(MEMBER_PROFILE_URL))
                                .activeStatus(ActiveStatus.valueOf((String) info.get(ACTIVE_STATUS)))
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .toList();

        } catch (DataAccessException e) {
            log.warn("Redis 접근 실패 (workSpaceSeq={})", workSpaceSeq);
            throw e;
        } catch (Exception e) {
            log.warn("Redis 조회 예외 (workSpaceSeq={}): {}", workSpaceSeq, e.getMessage());
            return Collections.emptyList();
        }
    }
}