package com.team404.synco.workspace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.service.RedisFallback;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.dto.WorkSpaceInfoResDto;
import com.team404.synco.workspace.dto.WorkSpaceMemberInfoResDto;
import com.team404.synco.workspace.entity.WorkSpace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final String ACTIVE_STATUS = "activeStatus";

    public WorkSpaceRedisService(
            @Qualifier("memberInventory") RedisTemplate<String, Object> memberRedisTemplate,
            @Qualifier("workSpaceInventory") RedisTemplate<String, Object> workSpaceRedisTemplate) {
        this.memberRedisTemplate = memberRedisTemplate;
        this.workSpaceRedisTemplate = workSpaceRedisTemplate;
    }

    // 멤버 기본 정보 저장
    public void addMemberInfo(Member member) {
        String memberKey = MEMBER_KEY_PREFIX + member.getMemberSeq();
        memberRedisTemplate.opsForHash().put(memberKey, MEMBER_NAME, member.getName());
        memberRedisTemplate.opsForHash().put(memberKey, MEMBER_PROFILE_URL, member.getProfileImageUrl());
        memberRedisTemplate.opsForHash().put(memberKey, ACTIVE_STATUS, member.getActiveStatus());
    }



    // 멤버 정보 변경
    // 멤버가 속한 프로젝트 목록 저장
    public void addWorkSpace(WorkSpace workSpace, Long memberSeq) {
        String memberKey = MEMBER_KEY_PREFIX + memberSeq;

        // workspaces 필드 가져오기
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

    // 프로젝트 정보 저장
    public void addMemberToWorkSpace(WorkSpace workSpace, Long memberSeq) {
        if (workSpace == null || workSpace.getWorkSpaceSeq() == null)
            throw new IllegalArgumentException("프로젝트 정보가 유효하지 않습니다.");

        String workSpaceKey = WORKSPACE_KEY_PREFIX + workSpace.getWorkSpaceSeq();

        try {
            // 프로젝트 기본정보 (문자열 그대로 저장)
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey, "name", workSpace.getWorkSpaceName());

            String startDate = workSpace.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey,"startDate", startDate);

            String endDate = workSpace.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey,"endDate", endDate);



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
        } catch (Exception e) {
            throw new SerializationException("프로젝트 Redis 저장 중 오류 발생", e);
        }
    }

    // 프로젝트 정보 수정
    public void editWorkSpaceInfo(Long workSpaceSeq, String name, String thumbnailImage){
        String workSpaceKey = WORKSPACE_KEY_PREFIX + workSpaceSeq;
        if(name != null){
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey, "name", name);
        }
        if (thumbnailImage != null) {
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey, "thumbnailImage",
                    thumbnailImage
            );
        }
    }

    // 프로젝트 목록 조회
    @RedisFallback
    public List<WorkSpaceInfoResDto> findMyWorkSpaceList(Long memberSeq) {
        try {
            String memberKey = MEMBER_KEY_PREFIX + memberSeq;
            Object cachedValue = memberRedisTemplate.opsForHash().get(memberKey, WORKSPACE_LIST);
            if (cachedValue == null) return Collections.emptyList();

            List<Long> workSpaceList = objectMapper.readValue(cachedValue.toString(), new TypeReference<>() {});
            if (workSpaceList.isEmpty()) return Collections.emptyList();

            return workSpaceList.stream()
                    .map(seq -> {
                        String workSpaceKey = WORKSPACE_KEY_PREFIX + seq;
                        Map<Object, Object> info = workSpaceRedisTemplate.opsForHash().entries(workSpaceKey);
                        if (info.isEmpty()) return null;

                        return WorkSpaceInfoResDto.builder()
                                .workSpaceSeq(seq)
                                .workSpaceName((String) info.get("name"))
                                .thumbnailImageUrl((String) info.get("thumbnailImage"))
                                .startDate(LocalDateTime.parse((String) info.get("startDate")))
                                .endDate(LocalDateTime.parse((String) info.get("endDate")))
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

    // 프로젝트 멤버 목록 조회
    @RedisFallback
    public List<WorkSpaceMemberInfoResDto> findWorkSpaceMemberList(Long workSpaceSeq, Long superMemberSeq) {
        try {
            String workSpaceKey = WORKSPACE_KEY_PREFIX + workSpaceSeq;
            Object cachedValue = workSpaceRedisTemplate.opsForHash().get(workSpaceKey, MEMBER_LIST);
            if (cachedValue == null) return Collections.emptyList();

            List<Long> memberList = objectMapper.readValue(cachedValue.toString(), new TypeReference<>() {});
            if (memberList.isEmpty()) return Collections.emptyList();

            return memberList.stream()
                    .map(seq -> {
                        String memberKey = MEMBER_KEY_PREFIX + seq;
                        Map<Object, Object> info = memberRedisTemplate.opsForHash().entries(memberKey);
                        if (info.isEmpty()) return null;

                        Authority authority = seq.equals(superMemberSeq)
                                ? Authority.SUPER
                                : Authority.PARTICIPANT;

                        return WorkSpaceMemberInfoResDto.builder()
                                .memberSeq(seq)
                                .name((String) info.get(MEMBER_NAME))
                                .profileImageUrl((String) info.get(MEMBER_PROFILE_URL))
                                .activeStatus(ActiveStatus.valueOf((String) info.get(ACTIVE_STATUS)))
                                .authority(authority)
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

    // 프로젝트를 멤버 정보에서 삭제
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

    // 멤버를 프로젝트 정보에서 삭제
    public void removeMemberFromWorkSpace(Long memberSeq, Long workSpaceSeq) throws Exception{
        String key = WORKSPACE_KEY_PREFIX + workSpaceSeq;
        String field = MEMBER_LIST;
        String json = (String) workSpaceRedisTemplate.opsForHash().get(key, field);
        if (json == null) return;

        List<Long> list = objectMapper.readValue(json, new TypeReference<>() {});
        list.remove(memberSeq);
        workSpaceRedisTemplate.opsForHash()
                .put(key, field, objectMapper.writeValueAsString(list));
    }

    // 프로젝트 목록에서 프로젝트 삭제
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