package com.team404.synco.chat.service;

import com.team404.synco.chat.dto.*;
import com.team404.synco.chat.dto.channel.*;
import com.team404.synco.chat.dto.channel.DelegateSuperAuthorityReqDto;
import com.team404.synco.chat.dto.channel.GrantAuthorityReqDto;
import com.team404.synco.chat.entity.*;
import com.team404.synco.chat.repository.ChatChannelMemberRepository;
import com.team404.synco.chat.repository.ChatChannelRepository;
import com.team404.synco.chat.repository.ChatMessageRepository;
import com.team404.synco.common.constant.Authority;
import com.team404.synco.common.service.MemberRedisComponent;
import com.team404.synco.common.service.S3Uploader;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatService {
    @Qualifier("memberInventory")
    private final RedisTemplate<String, Object> memberRedisTemplate;
    private final ChatChannelRepository chatChannelRepository;
    private final ChatChannelMemberRepository chatChannelMemberRepository;
    private final MemberRedisComponent memberRedisComponent;
    private final ChatMessageRepository chatMessageRepository;
    private final S3Uploader s3Uploader;
    private final String folderNamePrefix = "chat/";

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
        Optional.ofNullable(channelCreateReqDto.getMemberList()).orElse(Collections.emptyList())
                .stream().filter(Objects::nonNull).map(memberSeq -> ChatChannelMember.builder()
                        .memberSeq(memberSeq)
                        .authority(Authority.PARTICIPANT)
                        .chatChannel(chatChannel)
                        .build())
                .forEach(chatChannelMemberRepository::save);
        return chatChannel.getChatChannelSeq();
    }

    // 채널 생성(1:1 채팅 채널)
    // ToDo : 채팅 담당자는 1:1 채팅 시작할때 이 로직으로 채팅 채널 생성하시면 됩니다.
    public Long createIndividualChatChannel(ChannelCreateReqDto channelCreateReqDto) {
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 채팅 대상 list에 추가
        Optional.ofNullable(channelCreateReqDto.getMemberList()).orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(friendSeq -> ChatChannelMember.builder()
                        .memberSeq(friendSeq)
                        .chatChannel(chatChannel)
                        .authority(Authority.SUPER)
                        .build())
                .forEach(chatChannelMemberRepository::save);
        return chatChannel.getChatChannelSeq();
    }

    // 채널 생성(팀)
    public ChannelCreateResDto createChannel(ChannelCreateReqDto channelCreateReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 검증
        ChatChannel basicChannel = checkBasicChannel(channelCreateReqDto.getWorkSpaceSeq());

        // 권한 검증
        checkChannelAuthority(basicChannel.getChatChannelSeq(), memberSeq);

        // 새 채널 생성
        ChatChannel chatChannel = chatChannelRepository.save(channelCreateReqDto.toEntity());

        // 기존 채널 멤버를 새 채널에 추가
        List<ChatChannelMember> newMembers = basicChannel.getChatChannelmemberList().stream()
                .map(member -> ChatChannelMember.builder()
                        .memberSeq(member.getMemberSeq())
                        .chatChannel(chatChannel)
                        .build())
                .toList();

        chatChannelMemberRepository.saveAll(newMembers);

        return ChannelCreateResDto.fromEntity(chatChannel);
    }

    // 채널 이름 수정
    public ChannelEditResDto renameChannel(ChannelEditReqDto channelEditReqDto, Long memberSeq) throws AccessDeniedException {
        log.info("채널 번호 : {}", channelEditReqDto.getChannelSeq());
        log.info("채널 이름 : {}", channelEditReqDto.getChannelName());
        log.info("로그인한 사용자 : {}", memberSeq);

        // 수정 대상 채널 검증
        ChatChannel editChannel = chatChannelRepository.findById(channelEditReqDto.getChannelSeq()).orElseThrow(() ->
                new EntityNotFoundException("없는 채널입니다."));
        // 기본 채널 검증
        ChatChannel basicChannel = checkBasicChannel(editChannel.getWorkSpaceSeq());
        // 권한 검증
        checkChannelAuthority(basicChannel.getChatChannelSeq(), memberSeq);
        // 채널 수정
        editChannel.updateChannelName(channelEditReqDto.getChannelName());
        return ChannelEditResDto.fromEntity(editChannel);
    }

    // 채널 삭제
    public void deleteChannel(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        // 삭제 대상 채널 검증
        ChatChannel deleteChannel = chatChannelRepository.findById(channelSeq).orElseThrow(() ->
                new EntityNotFoundException("없는 채널입니다."));
        // 기본 채널이 있는지 검증
        ChatChannel basicChannel = checkBasicChannel(deleteChannel.getWorkSpaceSeq());
        // 삭제하려는 채널이 기본 채널인지 확인
        if (deleteChannel.equals(basicChannel)) {
            throw new IllegalStateException("기본 채널은 삭제할 수 없습니다.");
        }
        // 권한 검증
        checkChannelAuthority(basicChannel.getChatChannelSeq(), memberSeq);
        // 채널 삭제
        chatChannelRepository.deleteById(deleteChannel.getChatChannelSeq());
    }

    // 채널 권한 설정
    public ChannelGrantResDto grantToMember(GrantAuthorityReqDto grantAuthorityReqDto, Long memberSeq) throws AccessDeniedException {
        // 유효한 프로젝트인지 기본채널 여부를 통해 검증
        ChatChannel basicChannel = checkBasicChannel(grantAuthorityReqDto.getWorkSpaceSeq());
        // SUPER 권한 검증
        ChatChannelMember superMember = checkAuthorityIsSuper(basicChannel.getChatChannelSeq(), memberSeq);
        // 대상 멤버 조회
        ChatChannelMember grantMember = chatChannelMemberRepository.findByChannelAndMember
                (basicChannel.getChatChannelSeq(), grantAuthorityReqDto.getGrantMemberSeq()).orElseThrow(()
                -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        log.info("대상 멤버 소속 채널 : {}", grantMember.getChatChannel());
        // 권한 변경
        String authority = grantAuthorityReqDto.getAuthority();
        switch (authority) {
            case "MANAGER":
                grantMember.updateAuthority(Authority.MANAGER);
                break;
            case "PARTICIPANT":
                grantMember.updateAuthority(Authority.PARTICIPANT);
                break;
            default:
                break;
        }

        return ChannelGrantResDto.fromEntity(superMember, grantMember);
    }

    // 채널 SUPER 권한 위임
    @Transactional
    public void delegateSuperAuthority(DelegateSuperAuthorityReqDto delegateSuperAuthorityReqDto, Long memberSeq)
            throws AccessDeniedException {

        Long workSpaceSeq = delegateSuperAuthorityReqDto.getWorkSpaceSeq();
        Long delegateMemberSeq = delegateSuperAuthorityReqDto.getDelegateMemberSeq();

        // 기본 채널 조회 및 SUPER 권한 검증
        ChatChannel basicChannel = checkBasicChannel(workSpaceSeq);
        checkAuthorityIsSuper(basicChannel.getChatChannelSeq(), memberSeq);

        // 기본 채널에서만 권한 변경 수행
        // 위임 대상 멤버 권한을 SUPER로 변경
        chatChannelMemberRepository.findByChannelAndMember(basicChannel.getChatChannelSeq(), delegateMemberSeq)
                .ifPresent(changeAuthorityMember -> changeAuthorityMember.updateAuthority(Authority.SUPER));

        // 기존 SUPER(본인)의 권한을 PARTICIPANT로 변경
        chatChannelMemberRepository.findByChannelAndMember(basicChannel.getChatChannelSeq(), memberSeq)
                .ifPresent(prevSuperMember -> prevSuperMember.updateAuthority(Authority.PARTICIPANT));
    }

    // 모든 채널에 멤버 추가(WorkSpace에 처음 초대되었을때)
    public Long addMemberToChannel(ChannelInviteReqDto channelInviteReqDto, Long memberSeq) throws AccessDeniedException {
        // 기본 채널 조회 (권한 검증용)
        ChatChannel basicChannel = checkBasicChannel(channelInviteReqDto.getWorkSpaceSeq());

        // 초대한 사람 권한 검증
        checkChannelAuthority(basicChannel.getChatChannelSeq(), memberSeq);

        // 프로젝트 내 모든 채널 조회 (기본 채널 포함)
        List<ChatChannel> allChannels = chatChannelRepository
                .findByWorkSpaceSeqOrderByChatChannelSeqAsc(channelInviteReqDto.getWorkSpaceSeq());

        // 초대할 멤버들을 모든 채널에 추가
        return Optional.ofNullable(channelInviteReqDto.getMemberList())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .flatMap(teamMateSeq ->
                        allChannels.stream()
                                // 이미 채널에 존재하는 멤버는 건너뜀
                                .filter(channel -> !chatChannelMemberRepository
                                        .existsMember(channel.getChatChannelSeq(), teamMateSeq))
                                .map(channel -> {
                                    ChatChannelMember.ChatChannelMemberBuilder builder = ChatChannelMember.builder()
                                            .memberSeq(teamMateSeq)
                                            .chatChannel(channel);

                                    if (channel.getChatChannelSeq().equals(basicChannel.getChatChannelSeq())) {
                                        builder.authority(Authority.PARTICIPANT);
                                    }

                                    // 기본 채널이 아니면 authority = null 상태로 저장됨
                                    return builder.build();
                                })
                )
                .map(chatChannelMemberRepository::save)
                .count();
    }

    // 채널 리스트
    @Transactional(readOnly = true)
    public List<ChannelInfoResDto> findChatChannelList(Long workSpaceSeq) {
        // 모든 채널 조회
        List<ChatChannel> chatChannels = chatChannelRepository.findByWorkSpaceSeq(workSpaceSeq);

        if (chatChannels.isEmpty()) {
            return Collections.emptyList();
        }

        // 기본 채널 선택 (가장 먼저 생성된 채널)
        ChatChannel basicChannel = chatChannels.stream()
                .min(Comparator.comparing(ChatChannel::getChatChannelSeq))
                .orElseThrow(() -> new EntityNotFoundException("기본 채널이 존재하지 않습니다."));

        // 기본 채널 멤버 목록만 DTO로 변환
        List<ChannelMemberResDto> basicChannelMembers = basicChannel.getChatChannelmemberList().stream()
                .map(member -> {
                    String memberName = memberRedisComponent.getMemberName(member.getMemberSeq())
                            .replaceAll("^\"|\"$", "");
                    String memberProfileUrl = memberRedisComponent.getMemberProfileUrl(member.getMemberSeq())
                            .replaceAll("^\"|\"$", "");
                    return ChannelMemberResDto.of(member, memberName, memberProfileUrl);
                })
                .toList();

        // 모든 채널을 DTO로 변환하되, 기본 채널만 멤버 목록 포함
        return chatChannels.stream()
                .map(chatChannel -> {
                    List<ChannelMemberResDto> members =
                            chatChannel.getChatChannelSeq().equals(basicChannel.getChatChannelSeq())
                                    ? basicChannelMembers // 기본 채널: 멤버 포함
                                    : Collections.emptyList(); // 다른 채널: 멤버 없음

                    return ChannelInfoResDto.of(chatChannel, members);
                })
                .toList();
    }


    // 채널 전체 삭제(Team WorkSpace 삭제시)
    public void deleteAllChannel(Long workSpaceSeq) {
        chatChannelRepository.deleteAllByWorkSpaceSeq(workSpaceSeq);
    }

    // 프로젝트 탈퇴
    public void deleteMemberFromWorkSpace(Long workSpaceSeq, Long memberSeq){
        // 기본 채널 조회 (권한 검증용)
        ChatChannel basicChannel = checkBasicChannel(workSpaceSeq);
        log.info("권한 검증 성공");
        // 멤버가 채널에 있는지 확인
        chatChannelMemberRepository.findByChannelAndMember(basicChannel.getChatChannelSeq(),
                memberSeq).orElseThrow(() -> new EntityNotFoundException("프로젝트의 멤버가 아닙니다."));
        log.info("멤버 프로젝트 존재 여부 검증 성공");
        chatChannelMemberRepository.deleteByChannelAndMember(basicChannel.getChatChannelSeq(), memberSeq);
    }

    // 기본 채널 검증
    private ChatChannel checkBasicChannel(Long workSpaceSeq) {
        return chatChannelRepository.findFirstByWorkSpaceSeqOrderByChatChannelSeqAsc(workSpaceSeq).orElseThrow(() ->
                new EntityNotFoundException("기본 채널이 존재하지 않습니다. 유효하지 않은 WorkSpace입니다."));
    }

    // 채널 권한 검증
    private void checkChannelAuthority(Long channelSeq, Long memberSeq) throws AccessDeniedException {
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

    /////////////////////////////////////////// 채팅기능////////////////////////////////////////////////
    // 채팅참여자여부 확인 - stomphandler
    @Transactional(readOnly = true)
    public boolean isChannelParticipant(Long memberSeq, Long channelSeq) {
        ChatChannel channel = chatChannelRepository.findById(channelSeq)
                .orElseThrow(() -> new EntityNotFoundException("채널을 찾을 수 없습니다. channelSeq=" + channelSeq));

        return chatChannelMemberRepository.existsByChatChannelAndMemberSeq(channel, memberSeq);
    }

    // 메시지 저장 - stompcontroller
    @Transactional
    public ChatMessageResDto saveMessage(Long channelSeq, ChatMessageReqDto dto) {
        log.info("===========채팅 메시지 저장 시작===========");

        // 채널 검증
        ChatChannel chatChannel = chatChannelRepository.findById(channelSeq)
                .orElseThrow(() -> new EntityNotFoundException("채팅 채널을 찾을 수 없습니다. channelSeq=" + channelSeq));

        // Redis에서 발신자 정보 확인
        String memberKey = "memberSeq:" + dto.getSenderSeq();
        String rawMemberName = (String) memberRedisTemplate.opsForHash().get(memberKey, "memberName");
        String rawProfileUrl = (String) memberRedisTemplate.opsForHash().get(memberKey, "memberProfileUrl");

        // ✅ 따옴표 제거 (null-safe, JSON 문자열 대응)
        String memberName = rawMemberName != null ? rawMemberName.replaceAll("^\"|\"$", "") : null;
        String profileImageUrl = rawProfileUrl != null ? rawProfileUrl.replaceAll("^\"|\"$", "") : null;

        if (memberName == null) {
            throw new EntityNotFoundException("Redis에서 멤버 정보를 찾을 수 없습니다. memberSeq=" + dto.getSenderSeq());
        }

        // 채널 참여자 여부 확인
        ChatChannelMember sender = chatChannelMemberRepository
                .findByChatChannelAndMemberSeq(chatChannel, dto.getSenderSeq())
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 채널에 참여하지 않은 사용자입니다. memberSeq=" + dto.getSenderSeq()));

        // 파일 URL 문자열 그대로 저장
        String fileUrls = dto.getChatMessageFileUrls();

        // 메시지 엔티티 생성 및 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatChannelMember(sender)
                .chatMessageText(dto.getChatMessageText())
                .chatMessageFileUrls(fileUrls) // ← 그대로 저장
                .chatMessageParentSeq(dto.getReplyToSeq())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        log.info("💾 메시지 저장 완료 (channelSeq={}, memberSeq={}, memberName={}, files={}, chatMessageSeq={})",
                channelSeq, dto.getSenderSeq(), memberName, fileUrls, savedMessage.getChatMessageSeq());

        // ChatMessageResDto 생성하여 반환
        return ChatMessageResDto.builder()
                .chatMessageSeq(savedMessage.getChatMessageSeq())
                .channelSeq(channelSeq)
                .senderSeq(dto.getSenderSeq())
                .senderName(memberName)
                .senderProfileImageUrl(profileImageUrl)
                .messageType(dto.getMessageType())
                .chatMessageText(dto.getChatMessageText())
                .replyToSeq(dto.getReplyToSeq())
                .chatMessageFileUrls(fileUrls)
                .build();
    }

    // 첨부파일 저장
    public List<String> uploadChatFiles(Long channelSeq, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        // S3 경로 규칙: chat/{channelSeq}
        return s3Uploader.uploadAll(files, "chat/" + channelSeq);
    }

    // 채팅 참여자 목록 조회
    @Transactional(readOnly = true)
    public List<ChannelMemberResDto> getChannelMembers(Long channelSeq, Long memberSeq) throws AccessDeniedException {
        // 1️⃣ 접근 권한 확인
        if (!isChannelParticipant(memberSeq, channelSeq)) {
            throw new AccessDeniedException("채널 접근 권한이 없습니다.");
        }

        // 2️⃣ 채널 존재 확인
        ChatChannel channel = chatChannelRepository.findById(channelSeq)
                .orElseThrow(() -> new EntityNotFoundException("채널을 찾을 수 없습니다. channelSeq=" + channelSeq));

        // 3️⃣ 채널의 멤버 목록 조회
        List<ChatChannelMember> members = chatChannelMemberRepository.findByChatChannel(channel);

        // 4️⃣ Redis에서 memberName, profileImageUrl 조회
        return members.stream()
                .map(m -> {
                    String key = "memberSeq:" + m.getMemberSeq();
                    String rawName = (String) memberRedisTemplate.opsForHash().get(key, "memberName");
                    String rawProfileUrl = (String) memberRedisTemplate.opsForHash().get(key, "memberProfileUrl");

                    // 따옴표 제거 (Redis에 문자열이 JSON 형태로 저장된 경우)
                    String memberName = rawName != null ? rawName.replaceAll("^\"|\"$", "") : "알 수 없음";
                    String profileImageUrl = rawProfileUrl != null ? rawProfileUrl.replaceAll("^\"|\"$", "") : null;

                   return ChannelMemberResDto.builder()
                            .memberSeq(m.getMemberSeq())
                            .memberName(memberName)
                            .memberProfileUrl(profileImageUrl)
                            .build();
                })
                .toList();
    }

    // 채팅 메시지 삭제 (hard-delete)
    public void deleteChatMessage(Long chatMessageSeq, Long memberSeq) throws AccessDeniedException {
        // 메시지 존재 여부 확인
        ChatMessage chatMessage = chatMessageRepository.findById(chatMessageSeq)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다. chatMessageSeq=" + chatMessageSeq));

        // 본인 메시지인지 확인
        if (memberSeq == null ||  chatMessage.getChatChannelMember().getMemberSeq() != memberSeq) {
            throw new AccessDeniedException("자신이 보낸 메시지만 삭제할 수 있습니다.");
        }

        // 삭제
        chatMessageRepository.delete(chatMessage);

        log.info("💥 메시지 영구 삭제 완료 - chatMessageSeq={}, memberSeq={}", chatMessageSeq, memberSeq);
    }

    // 채팅목록 조회 (개인워크스페이스)
    @Transactional(readOnly = true)
    public List<MyChatListResDto> getMyChatChannelsByWorkspace(Long memberSeq, WorkSpaceType workSpaceType) {
        List<ChatChannelMember> chatChannelMembers = chatChannelMemberRepository
                .findByMemberSeqAndChatChannel_WorkSpaceType(memberSeq, workSpaceType);
        return mapToDtoList(chatChannelMembers);
    }

//    // 채팅목록 조회 (프로젝트워크스페이스)
//    @Transactional(readOnly = true)
//    public List<MyChatListResDto> getMyChatChannelsByProjectWorkspace(Long memberSeq, Long workspaceSeq) {
//        List<ChatChannelMember> chatChannelMembers = chatChannelMemberRepository
//                .findByMemberSeqAndChatChannel_WorkSpaceSeq(memberSeq, workspaceSeq);
//        return mapToDtoList(chatChannelMembers);
//    }

    // 채널목록 조회용 공통 DTO 매핑
    private List<MyChatListResDto> mapToDtoList(List<ChatChannelMember> chatChannelMembers) {
        List<MyChatListResDto> dtos = new ArrayList<>();

        for (ChatChannelMember m : chatChannelMembers) {
            ChatChannel channel = m.getChatChannel();
            Long lastReadSeq = m.getLastReadChatMessageSeq();

            // 읽지 않은 메시지 개수 계산
            Long unreadCount = (lastReadSeq == null)
                    ? chatMessageRepository.countByChatChannelMember_ChatChannel(channel)
                    : chatMessageRepository.countByChatChannelMember_ChatChannelAndChatMessageSeqGreaterThan(channel,
                            lastReadSeq);

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
