package com.team404.synco.chat.service;

import com.team404.synco.chat.dto.ChannelCreateReqDto;
import com.team404.synco.chat.dto.ChannelInviteReqDto;
import com.team404.synco.chat.dto.DelegateSuperAuthorityReqDto;
import com.team404.synco.chat.dto.GrantAuthorityReqDto;
import com.team404.synco.chat.dto.*;
import com.team404.synco.chat.entity.ChatChannel;
import com.team404.synco.chat.entity.ChatChannelMember;
import com.team404.synco.chat.entity.ChatMessage;
import com.team404.synco.chat.entity.WorkSpaceType;
import com.team404.synco.chat.repository.ChatChannelMemberRepository;
import com.team404.synco.chat.repository.ChatChannelRepository;
import com.team404.synco.chat.repository.ChatMessageRepository;
import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.constant.YnColumn;
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
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.*;

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
    private final String folderNamePrefix = "chat/";

    public ChatService(RedisTemplate<String, Object> memberRedisTemplate, ChatChannelRepository chatChannelRepository, ChatChannelMemberRepository chatChannelMemberRepository, ChatMessageRepository chatMessageRepository, S3Uploader s3Uploader, ChatRedisService chatRedisService) {
        this.memberRedisTemplate = memberRedisTemplate;
        this.chatChannelRepository = chatChannelRepository;
        this.chatChannelMemberRepository = chatChannelMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.s3Uploader = s3Uploader;
        this.chatRedisService = chatRedisService;
    }

    // 기본 채널 생성
    public Long createBasicChannel(ChannelCreateReqDto channelCreateReqDto) {
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채널 생성자 권한 부여 및 저장
        ChatChannelMember creator = ChatChannelMember.builder()
                .memberSeq(channelCreateReqDto.getMemberSeq())
                .authority(Authority.SUPER)
                .chatChannel(chatChannel)
                .build();
        chatChannelMemberRepository.save(creator);

        Optional.ofNullable(channelCreateReqDto.getFriendList()).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(memberSeq -> ChatChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build())
                .forEach(chatChannelMemberRepository::save);
        return chatChannel.getChatChannelSeq();
    }


    // 채널 생성
    public Long createChannel(ChannelCreateReqDto channelCreateReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널이 있는지 검증
        ChatChannel basicChannel = chatChannelRepository.
                findFirstByWorkSpaceSeqOrderByChatChannelSeqAsc(channelCreateReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                        new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));
        // 권한 검증
        checkInviteAndCreateChannelAuthority(basicChannel.getChatChannelSeq(), memberSeq);

        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채널 생성자 권한 부여 및 저장
        ChatChannelMember creator = ChatChannelMember.builder()
                .memberSeq(memberSeq)
                .authority(Authority.MANAGER)
                .chatChannel(chatChannel)
                .build();
        chatChannelMemberRepository.save(creator);

        Optional.ofNullable(channelCreateReqDto.getFriendList()).orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(friendSeq -> ChatChannelMember.builder()
                        .memberSeq(friendSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build())
                .forEach(chatChannelMemberRepository::save);
        return chatChannel.getChatChannelSeq();
    }

    // 채널 권한 설정
    public void grantToMember(GrantAuthorityReqDto grantAuthorityReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널이 있는지 검증
        ChatChannel basicChannel = chatChannelRepository.
                findFirstByWorkSpaceSeqOrderByChatChannelSeqAsc(grantAuthorityReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                        new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));
        // SUPER 권한 검증
        checkAuthorityIsSuper(basicChannel.getChatChannelSeq(), memberSeq);
        // 대상 멤버 조회
        ChatChannelMember changeAuthorityMember = chatChannelMemberRepository.findByChannelAndMember
                (grantAuthorityReqDto.getChannelSeq(), grantAuthorityReqDto.getGrantMemberSeq()).orElseThrow(()
                -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        // 권한 변경
        String authority = String.valueOf(changeAuthorityMember.getAuthority());
        switch (authority) {
            case "MANAGER":
                changeAuthorityMember.updateAuthority(Authority.MANAGER);
            case "PARTICIPANT":
                changeAuthorityMember.updateAuthority(Authority.PARTICIPANT);
            default:
                break;
        }
    }

    // 채널 SUPER 권한 위임
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq)
            throws AccessDeniedException {
        // SUPER 권한 검증
        ChatChannelMember superAuthorityMember = checkAuthorityIsSuper(delegateSuperAuthorityReqDto.getWorkSpaceSeq(), memberSeq);
        // 대상 멤버 조회
        ChatChannelMember changeAuthorityMember = chatChannelMemberRepository.findByChannelAndMember
                (memberSeq, delegateSuperAuthorityReqDto.getWorkSpaceSeq()).orElseThrow(()
                -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다.."));
        // 위임할 사용자의 권한을 SUPER로 변경
        changeAuthorityMember.updateAuthority(Authority.SUPER);
        // 현재 사용자의 권한을 참여자로 변경
        superAuthorityMember.updateAuthority(Authority.PARTICIPANT);
    }

    // 멤버 추가
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto, Long memberSeq) throws AccessDeniedException {
        ChatChannel checkIsFirstChannel = chatChannelRepository.
                findFirstByWorkSpaceSeqOrderByChatChannelSeqAsc(channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                        new EntityNotFoundException("기본 채널이 존재하지 않습니다."));
        // 권한 검증
        checkInviteAndCreateChannelAuthority(checkIsFirstChannel.getChatChannelSeq(), memberSeq);

        ChatChannel chatChannel;
        Long channelSeq = channelInviteReqDto.getChannelSeq();
        // 채널 번호가 있으면 해당 채널로 설정
        if (channelSeq != null && channelSeq > 0) {
            chatChannel = chatChannelRepository.findByChatChannelSeqAndWorkSpaceSeq(channelInviteReqDto.getChannelSeq(),
                    channelInviteReqDto.getWorkSpaceSeq()).orElseThrow(() ->
                    new EntityNotFoundException("등록되지 않은 채널입니다."));
            channelInviteReqDto.setChannelSeq(chatChannel.getChatChannelSeq());
            // 없으면 기본 채널로 설정
        } else {
            chatChannel = checkIsFirstChannel;
        }

        return Optional.ofNullable(channelInviteReqDto.getFriendList())
                .orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .peek(teamMateSeq -> {
                    // 🔥 중복 멤버 예외 처리
                    if (chatChannelMemberRepository.existsMember(chatChannel.getChatChannelSeq(),
                            teamMateSeq)) {
                        throw new IllegalStateException("이미 채널에 존재하는 멤버입니다: " + teamMateSeq);
                    }
                })
                .map(teamMateSeq -> ChatChannelMember.builder()
                        .memberSeq(teamMateSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build())
                .map(chatChannelMemberRepository::save)
                .count();
    }

    // 채널 전체 삭제(WorkSpace 삭제시)
    public void deleteAllChannel(Long workSpaceSeq) {
        chatChannelRepository.deleteAllByWorkSpaceSeq(workSpaceSeq);
    }


    // 초대, 채널 생성 권한 검증
    private void checkInviteAndCreateChannelAuthority(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        ChatChannelMember chatChannelMember = chatChannelMemberRepository.findByChannelAndMember(channelSeq,
                memberSeq).orElseThrow(() -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));

        if (!chatChannelMember.getAuthority().equals(Authority.SUPER) &&
                !chatChannelMember.getAuthority().equals(Authority.MANAGER)) {
            throw new AccessDeniedException("초대 권한이 없습니다.");
        }
    }

    // SUPER 권한 검증
    private ChatChannelMember checkAuthorityIsSuper(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 멤버 조회
        ChatChannelMember chatChannelMember = chatChannelMemberRepository.
                findByChannelAndMember(channelSeq, memberSeq).orElseThrow(()
                        -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        // 현재 사용자의 권한이 super인지 확인
        if (!chatChannelMember.getAuthority().equals(Authority.SUPER)) {
            throw new AccessDeniedException("SUPER 사용자만 권한 변경이 가능합니다.");
        }
        return chatChannelMember;
    }

    ///////////////////////////////////////////채팅기능////////////////////////////////////////////////
    // 채팅참여자여부 확인 - stomphandler
    @Transactional(readOnly = true)
    public boolean isChannelParticipant(Long memberSeq, Long channelSeq) {
        ChatChannel channel = chatChannelRepository.findById(channelSeq)
                .orElseThrow(() -> new EntityNotFoundException("채널을 찾을 수 없습니다. channelSeq=" + channelSeq));

        return chatChannelMemberRepository.existsByChatChannelAndMemberSeq(channel, memberSeq);
    }

    // 메시지 저장 - stompcontroller
    public void saveMessage(Long channelSeq, ChatMessageReqDto dto) {
        // 1️⃣ 채널 존재 여부 검증
        log.info("===========채널존재여부검증===========");
        ChatChannel chatChannel = chatChannelRepository.findById(channelSeq)
                .orElseThrow(() -> new EntityNotFoundException("채팅 채널을 찾을 수 없습니다. channelSeq=" + channelSeq));

        // 2️⃣ 발신자 존재 여부 검증 (Redis에서 조회)
        log.info("===========발신자존재여부검증===========");
        String memberKey = "memberSeq:" + dto.getSenderSeq();
        String memberName = (String) memberRedisTemplate.opsForHash().get(memberKey, "memberName");

        if (memberName == null) {
            throw new EntityNotFoundException("Redis에서 멤버 정보를 찾을 수 없습니다. memberSeq=" + dto.getSenderSeq());
        }

        // 3️⃣ 해당 채팅 채널의 참여자 여부 검증
        log.info("===========채팅채널의참여자여부검증===========");
        ChatChannelMember sender = chatChannelMemberRepository
                .findByChatChannelAndMemberSeq(chatChannel, dto.getSenderSeq())
                .orElseThrow(() -> new EntityNotFoundException("해당 채널에 참여하지 않은 사용자입니다. memberSeq=" + dto.getSenderSeq()));

        // 4️⃣ 파일 업로드 처리 (optional)
        // TODO: s3업로드는 api로 하고 이거는 db에 url 저장하는 로직으로 바꿔야함
        log.info("===========파일업로드처리===========");
        String fileUrls = null;
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            if (dto.getFiles().size() > 20) {
                throw new IllegalArgumentException("최대 20개의 파일만 전송할 수 있습니다.");
            }

//            List<String> uploadedUrls = s3Uploader.upload(dto.getFiles(), "chat");
//            fileUrls = String.join(",", uploadedUrls);
        }

        // 5️⃣ 메시지 엔티티 생성
        log.info("===========메시지 엔티티 생성===========");
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

    // 채팅 파일 업로드 (폴더 자동 생성 + 중복 검증)
    public List<String> uploadFiles(List<MultipartFile> files, Long chatChannelSeq) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // 예: chat/15
                String uploadPath = folderNamePrefix + chatChannelSeq;
                String fileUrl = s3Uploader.upload(file, uploadPath);
                uploadedUrls.add(fileUrl);
            } catch (Exception e) {
                log.error("❌ 채팅 파일 업로드 실패: {}", file.getOriginalFilename(), e);
            }
        }

        log.info("💬 ChatService - 업로드 완료 (channelSeq={}): {}", chatChannelSeq, uploadedUrls);
        return uploadedUrls;
    }

    // Presigned URL 생성 (다운로드용)
    public String generateDownloadUrl(String key) {
        return s3Uploader.createPresignedUrl(key);
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
        List<ChatChannelMember> chatChannelMembers =
                chatChannelMemberRepository.findByMemberSeqAndChatChannel_WorkSpaceSeq(memberSeq, workspaceSeq);
        return mapToDtoList(chatChannelMembers);
    }

    // 채널목록 조회용 공통 DTO 매핑
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

    // 이전 메시지 조회
    // 채팅메시지 읽음처리
    // 채널 나가기
    // 1:1채팅방 개설 또는 기존 channelSeq return
