package com.team404.synco.chat.service;

import com.team404.synco.chat.dto.*;
import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.chat.entity.ChatMessage;
import com.team404.synco.chat.entity.WorkSpaceType;
import com.team404.synco.chat.repository.ChatChannelMemberRepository;
import com.team404.synco.chat.repository.ChatChannelRepository;
import com.team404.synco.chat.repository.ChatMessageRepository;
import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.service.S3Uploader;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
public class ChatService {
    @Qualifier("memberInventory")
    private final RedisTemplate<String, Object> memberRedisTemplate;
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final S3Uploader s3Uploader;
    private final ChatRedisService chatRedisService;

    public ChatService(RedisTemplate<String, Object> memberRedisTemplate, ChatChannelRepository chatChannelRepository, ChatChannelMemberRepository chatChannelMemberRepository, ChatMessageRepository chatMessageRepository, S3Uploader s3Uploader, ChatRedisService chatRedisService) {
        this.memberRedisTemplate = memberRedisTemplate;
        this.chatChannelRepository = chatChannelRepository;
        this.chatChannelMemberRepository = chatChannelMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.s3Uploader = s3Uploader;
        this.chatRedisService = chatRedisService;
    }

    // 채널 생성
    // ToDO : 우선은 기본 채널 생성만 작업했습니다. 추후 채널 추가 가능하도록 코드 수정 예정입니다.
    public Long createChannel(ChannelCreateReqDto channelCreateReqDto) {
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채널 생성자 권한 부여 및 저장
        ChatChannelMember creator = ChatChannelMember.builder()
                .memberSeq(channelCreateReqDto.getMemberSeq())
                .authority(Authority.SUPER)
                .chatChannel(chatChannel)
                .build();
        chatChannelMemberRepository.save(creator);

        List<Long> friendList = channelCreateReqDto.getFriendList();
        if (friendList != null && !friendList.isEmpty()) {
            for (Long memberSeq : friendList) {
                ChatChannelMember chatChannelMember = ChatChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build();
                chatChannelMemberRepository.save(chatChannelMember);
            }
        }

        return chatChannel.getChatChannelSeq();
    }

    // 멤버 추가
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto) {
        ChatChannel chatChannel = chatChannelRepository.findByChatChannelSeqAndWorkSpaceSeq(channelInviteReqDto.getChannelSeq(),
                channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                new EntityNotFoundException("등록되지 않은 채널입니다."));
        List<Long> friendList = channelInviteReqDto.getFriendList();
        for (Long memberSeq : friendList) {
            ChatChannelMember chatChannelMember = ChatChannelMember.builder()
                    .memberSeq(memberSeq)
                    .authority(Authority.PARTICIPANT)
                    .chatChannel(chatChannel)
                    .build();
            chatChannelMemberRepository.save(chatChannelMember);
        }
        return (long) channelInviteReqDto.getFriendList().size();
    }

    // 채팅참여자여부 확인 - stomphandler
    @Transactional(readOnly = true)
    public boolean isChannelParticipant(Long memberSeq, Long channelSeq) {
        ChatChannel channel = chatChannelRepository.findById(channelSeq)
                .orElseThrow(() -> new EntityNotFoundException("채널을 찾을 수 없습니다. channelSeq=" + channelSeq));

        return chatChannelMemberRepository.existsByChatChannelAndMemberSeq(channel, memberSeq);
    }

    // 메시지 저장 - stompcontroller
    public void saveMessage(Long channelSeq, ChatMessageReqDto dto) {
        // 1️⃣ 채널 조회
        ChatChannel chatChannel = chatChannelRepository.findById(channelSeq)
                .orElseThrow(() -> new EntityNotFoundException("채팅 채널을 찾을 수 없습니다. channelSeq=" + channelSeq));

        // 2️⃣ 발신자 정보 Redis에서 조회
        String memberKey = "memberSeq:" + dto.getSenderSeq();
        String memberName = (String) memberRedisTemplate.opsForHash().get(memberKey, "memberName");
        String profileImageUrl = (String) memberRedisTemplate.opsForHash().get(memberKey, "memberProfileUrl");

        if (memberName == null) {
            throw new EntityNotFoundException("Redis에서 멤버 정보를 찾을 수 없습니다. memberSeq=" + dto.getSenderSeq());
        }

        // 3️⃣ ChatChannelMember 조회 (채널+멤버 조합)
        ChatChannelMember sender = chatChannelMemberRepository
                .findByChatChannelAndMemberSeq(chatChannel, dto.getSenderSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 채널에 참여하지 않은 사용자입니다. memberSeq=" + dto.getSenderSeq()));

        // 4️⃣ 파일 업로드 처리 (optional)
        String fileUrls = null;
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            if (dto.getFiles().size() > 20) {
                throw new IllegalArgumentException("최대 20개의 파일만 전송할 수 있습니다.");
            }

            List<String> uploadedUrls = s3Uploader.upload(dto.getFiles(), "chat");
            fileUrls = String.join(",", uploadedUrls);
        }

        // 5️⃣ 메시지 엔티티 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .chatChannelMember(sender)
                .chatMessageText(dto.getChatMessageText())
                .chatMessageFileUrls(fileUrls)
                .chatMessageParentSeq(dto.getReplyToSeq() != null ? dto.getReplyToSeq() : 0L)
                .build();

        chatMessageRepository.save(chatMessage);

        // 6️⃣ 로그
        log.info("💾 메시지 저장 완료 (channelSeq={}, memberSeq={}, memberName={})",
                channelSeq, dto.getSenderSeq(), memberName);
    }

    // 채팅목록 조회 (개인워크스페이스)
    @Transactional(readOnly = true)
    public List<MyChatListResDto> getMyChatChannelsByWorkspace(Long memberSeq, WorkSpaceType workSpaceType) {
        List<ChatChannelMember> chatChannelMembers =
                chatChannelMemberRepository.findByMemberSeqAndChatChannel_WorkSpaceType(memberSeq, workSpaceType);
        return mapToDtoList(chatChannelMembers);
    }

    // 채팅목록 조회 (프로젝트워크스페이스)
    @Transactional(readOnly = true)
    public List<MyChatListResDto> getMyChatChannelsByProjectWorkspace(Long memberSeq, Long workspaceSeq) {
        List<ChatChannelMember> memberships =
                chatChannelMemberRepository.findByMemberSeqAndChatChannel_WorkSpaceSeq(memberSeq, workspaceSeq);
        return mapToDtoList(memberships);
    }

    // 공통 DTO 매핑
    private List<MyChatListResDto> mapToDtoList(List<ChatChannelMember> chatChannelMembers) {
        List<MyChatListResDto> dtos = new ArrayList<>();

        for (ChatChannelMember m : chatChannelMembers) {
            ChatChannel channel = m.getChatChannel();
            Long lastReadSeq = m.getLastReadChatMessageSeq();

            // 읽지 않은 메시지 개수 계산
            Long unreadCount = (lastReadSeq == null)
                    ? chatMessageRepository.countByChatChannelMember_ChatChannel(channel)
                    : chatMessageRepository.countByChatChannelMember_ChatChannelAndChatMessageSeqGreaterThan(channel, lastReadSeq);

            dtos.add(MyChatListResDto.builder()
                    .channelSeq(channel.getChatChannelSeq())
                    .channelName(channel.getChatChannelName())
                    .workspaceSeq(channel.getWorkSpaceSeq())
                    .workSpaceType(channel.getWorkSpaceType())
                    .unreadCount(unreadCount)
                    .isGroupChat(channel.getWorkSpaceType() == WorkSpaceType.PROJECT)
                    .build());
        }
        return dtos;
    }
}

    // 내 채팅목록 조회
    // 이전 메시지 조회
    // 채팅메시지 읽음처리
    // 채널 나가기
    // 1:1채팅방 개설 또는 기존 channelSeq return
