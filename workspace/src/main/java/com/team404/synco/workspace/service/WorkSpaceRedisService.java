package com.team404.synco.workspace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.common.service.RedisFallback;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.dto.WorkSpaceInfoDto;
import com.team404.synco.workspace.entity.WorkSpace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class WorkSpaceRedisService {
    private final RedisTemplate<String, Object> memberRedisTemplate;
    private final RedisTemplate<String, Object> workSpaceRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MEMBER_KEY_PREFIX = "memberSeq:";
    private static final String WORKSPACE_KEY_PREFIX = "workSpaceSeq:";
    private static final String WORKSPACE_LIST = "workSpaceList";
    private static final String MEMBER_LIST = "memberList";
    private static final String MEMBER_NAME = "memberName";
    private static final String MEMBER_PROFILE_URL = "memberProfileUrl";

    public WorkSpaceRedisService(
            @Qualifier("memberInventory") RedisTemplate<String, Object> memberRedisTemplate,
            @Qualifier("workSpaceInventory") RedisTemplate<String, Object> workSpaceRedisTemplate) {
        this.memberRedisTemplate = memberRedisTemplate;
        this.workSpaceRedisTemplate = workSpaceRedisTemplate;
    }

    // 멤버 기본 정보 저장
    public void addMemberInfo(Member member) {
        String memberKey = MEMBER_KEY_PREFIX + member.getMemberSeq();
        if (!memberRedisTemplate.hasKey(memberKey)) {
            memberRedisTemplate.opsForHash().put(memberKey, MEMBER_NAME, member.getName());
            memberRedisTemplate.opsForHash().put(memberKey, MEMBER_PROFILE_URL, member.getProfileImageUrl());
        }
    }

    // 멤버가 속한 워크스페이스 목록 저장
    public void addWorkSpace(WorkSpace workSpace, Long memberSeq) {
        String memberKey = MEMBER_KEY_PREFIX + memberSeq;
        Object existing = memberRedisTemplate.opsForHash().get(memberKey, WORKSPACE_LIST);
        List<Long> workSpaces = new ArrayList<>();

        try {
            if (existing != null)
                workSpaces = objectMapper.readValue(existing.toString(), new TypeReference<>() {});
            if (!workSpaces.contains(workSpace.getWorkSpaceSeq()))
                workSpaces.add(workSpace.getWorkSpaceSeq());
            memberRedisTemplate.opsForHash()
                    .put(memberKey, WORKSPACE_LIST, objectMapper.writeValueAsString(workSpaces));
        } catch (Exception e) {
            throw new SerializationException("workSpaceList 직렬화 실패", e);
        }
    }

    // 워크스페이스 정보 저장
    public void addMemberToWorkSpace(WorkSpace workSpace, Long memberSeq) {
        if (workSpace == null || workSpace.getWorkSpaceSeq() == null)
            throw new IllegalArgumentException("워크스페이스 정보가 유효하지 않습니다.");

        String workSpaceKey = WORKSPACE_KEY_PREFIX + workSpace.getWorkSpaceSeq();

        try {
            // 워크스페이스 기본정보 (문자열 그대로 저장)
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey, "name", workSpace.getWorkSpaceName());

            if (workSpace.getWorkSpaceThumbnailImageUrl() != null) {
                workSpaceRedisTemplate.opsForHash().put(
                        workSpaceKey,
                        "thumbnailImage",
                        workSpace.getWorkSpaceThumbnailImageUrl()
                );
            }

            // 멤버 목록 갱신
            Object existing = workSpaceRedisTemplate.opsForHash().get(workSpaceKey, MEMBER_LIST);
            List<Long> memberList = new ArrayList<>();

            if (existing != null) {
                memberList = objectMapper.readValue(existing.toString(), new TypeReference<>() {});
            }

            if (memberSeq != null && !memberList.contains(memberSeq)) {
                memberList.add(memberSeq);
            }

            // JSON 문자열로 저장
            String json = objectMapper.writeValueAsString(memberList);
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey, MEMBER_LIST, json);

            log.debug("워크스페이스 [{}] 정보 저장 완료 (name, thumbnail, memberList)", workSpace.getWorkSpaceSeq());

        } catch (Exception e) {
            throw new SerializationException("워크스페이스 Redis 저장 중 오류 발생", e);
        }
    }

    // 워크스페이스 목록 조회
    @RedisFallback
    public List<WorkSpaceInfoDto> findMyWorkSpaceList(Long memberSeq) {
        if (memberSeq == null) throw new IllegalArgumentException("memberSeq가 null입니다.");

        try {
            String memberKey = MEMBER_KEY_PREFIX + memberSeq;
            Object cachedValue = memberRedisTemplate.opsForHash().get(memberKey, WORKSPACE_LIST);
            if (cachedValue == null) return Collections.emptyList();

            List<Long> workSpaceSeqList = objectMapper.readValue(cachedValue.toString(), new TypeReference<>() {});
            if (workSpaceSeqList.isEmpty()) return Collections.emptyList();

            return workSpaceSeqList.stream()
                    .map(seq -> {
                        String workSpaceKey = WORKSPACE_KEY_PREFIX + seq;
                        Map<Object, Object> info = workSpaceRedisTemplate.opsForHash().entries(workSpaceKey);
                        if (info.isEmpty()) return null;

                        return WorkSpaceInfoDto.builder()
                                .workSpaceSeq(seq)
                                .workSpaceName((String) info.get("name"))
                                .thumbnailImageUrl((String) info.get("thumbnailImage"))
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .toList();

        } catch (DataAccessException e) {
            log.warn("Redis 접근 실패 (memberSeq={})", memberSeq);
            throw e;
        } catch (Exception e) {
            log.warn("Redis 조회 예외 (memberSeq={}): {}", memberSeq, e.getMessage());
            return Collections.emptyList();
        }
    }

    // 워크스페이스를 멤버 정보에서 삭제
    public void removeWorkspaceFromMember(Long memberSeq, Long workspaceSeq) throws Exception {
        String key = MEMBER_KEY_PREFIX + memberSeq;
        String field = WORKSPACE_LIST;
        String json = (String) memberRedisTemplate.opsForHash().get(key, field);
        if (json == null) return;

        List<Long> list = objectMapper.readValue(json, new TypeReference<>() {});
        list.remove(workspaceSeq);
        memberRedisTemplate.opsForHash()
                .put(key, field, objectMapper.writeValueAsString(list));
    }

    // 워크스페이스 목록에서 워크스페이스 삭제
    public void removeWorkspace(Long workspaceSeq) throws Exception {
        workSpaceRedisTemplate.delete(WORKSPACE_KEY_PREFIX + workspaceSeq);
    }

    // 친구 목록 조회
    // ToDo : 개발중
    /*public List<Long> getFriendList(Long workSpaceSeq) {
        String key = "workSpaceSeq:" + workSpaceSeq;
        Object redisValue = workSpaceRedisTemplate.opsForHash().get(key, "memberList");

        if (redisValue == null) return new ArrayList<>();

        try {
            String json = redisValue.toString();
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = json.substring(1, json.length() - 1);
            }
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            throw new SerializationException("Redis memberList 역직렬화 실패", e);
        }
    }*/
}