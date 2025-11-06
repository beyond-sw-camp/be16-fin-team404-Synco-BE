package com.team404.synco.virtualmeeting.dto.Room;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RoomDetailDto {
    private Long roomId;
    private String roomName;
    private String roomDescription;
    private Long hostId;
    private LocalDateTime createdAt; // 방 생성 시간
    private Long duration;    // 방 지속 시간
    private String summaryContent; // 회의 요약 내용
    private String downloadUrl; // 녹화 파일 다운로드 URL
    private List<ParticipantDto> participants; // 참가자 목록
    private Integer participantCount; // 참가자 수


    @Getter
    @Builder
    public static class ParticipantDto {
        private Long participantId;
        private String participantProfileUrl;
        private String participantName;
        private String participantStatus;
    }
}
