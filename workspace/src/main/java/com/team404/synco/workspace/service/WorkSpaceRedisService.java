package com.team404.synco.workspace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team404.synco.member.entity.Member;
import com.team404.synco.workspace.entity.WorkSpace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WorkSpaceRedisService {
    private final RedisTemplate<String, Object> memberRedisTemplate;
    private final RedisTemplate<String, Object> workSpaceRedisTemplate;
    private static final String MEMBER_KEY_PREFIX = "memberSeq:";
    private static final String WORKSPACE_KEY_PREFIX = "workSpaceSeq:";
    private static final String WORKSPACE_LIST = "workSpaceList";
    private static final String MEMBER_LIST = "memberList";
    private static final String MEBMER_NAME = "memberName";
    private static final String MEMBER_PROFILE_URL = "memberProfileUrl";

    public WorkSpaceRedisService(@Qualifier("memberInventory") RedisTemplate<String, Object> memberRedisTemplate, @Qualifier("workSpaceInventory")RedisTemplate<String, Object> workSpaceRedisTemplate) {
        this.memberRedisTemplate = memberRedisTemplate;
        this.workSpaceRedisTemplate = workSpaceRedisTemplate;
    }

    // 멤버 정보 redis에 추가
    public void addMemberInfo(Member member) {
        String memberKey = MEMBER_KEY_PREFIX + member.getMemberSeq();

        // 기존 member key 존재 여부 확인
        Boolean hasKey = memberRedisTemplate.hasKey(memberKey);

        // key가 없을 경우 → 멤버 정보 처음 등록
        if (!hasKey) {
            memberRedisTemplate.opsForHash().put(memberKey, MEBMER_NAME, member.getName());
            memberRedisTemplate.opsForHash().put(memberKey, MEMBER_PROFILE_URL, member.getProfileImageUrl());
        }
    }

    // 워크스페이스 정보 redis에 추가
    public void addWorkSpace(WorkSpace workSpace, Long memberSeq) {
        String memberKey = MEMBER_KEY_PREFIX + memberSeq;

        // workspaces 필드 가져오기
        Object existing = memberRedisTemplate.opsForHash().get(memberKey, WORKSPACE_LIST);
        List<Long> workSpaces = new ArrayList<>();

        if (existing != null) {
            try {
                workSpaces = new ObjectMapper().readValue(existing.toString(), new TypeReference<List<Long>>() {
                });
            } catch (Exception e) {
                throw new SerializationException("직렬화에 실패하였습니다.");
            }
        }

        // 새로운 workspaceSeq 리스트에 추가
        if (!workSpaces.contains(workSpace.getWorkSpaceSeq())) {
            workSpaces.add(workSpace.getWorkSpaceSeq());
        }

        try {
            String json = new ObjectMapper().writeValueAsString(workSpaces);
            memberRedisTemplate.opsForHash().put(memberKey, WORKSPACE_LIST, json);
        } catch (Exception e) {
            throw new SerializationException("직렬화에 실패하였습니다.");
        }
    }

    // 워크스페이스에 초대된 멤버 redis에 추가
    public void addMemberToWorkSpace(WorkSpace workSpace, Long memberSeq) {
        String workSpaceKey = WORKSPACE_KEY_PREFIX + workSpace.getWorkSpaceSeq();

        // memberList 필드 가져오기
        Object existing = workSpaceRedisTemplate.opsForHash().get(workSpaceKey, MEMBER_LIST);
        List<Long> memberList = new ArrayList<>();

        if (existing != null) {
            try {
                memberList = new ObjectMapper().readValue(existing.toString(), new TypeReference<List<Long>>() {
                });
            } catch (Exception e) {
                throw new SerializationException("직렬화에 실패하였습니다.");
            }
        }

        // 새로운 member 리스트에 추가
        if (!memberList.contains(memberSeq)) {
            memberList.add(memberSeq);
        }

        try {
            String json = new ObjectMapper().writeValueAsString(memberList);
            workSpaceRedisTemplate.opsForHash().put(workSpaceKey, MEMBER_LIST, json);
        } catch (Exception e) {
            throw new SerializationException("직렬화에 실패하였습니다.");
        }
    }
}
