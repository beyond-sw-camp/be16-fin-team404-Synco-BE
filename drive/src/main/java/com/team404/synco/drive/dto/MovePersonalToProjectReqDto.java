package com.team404.synco.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovePersonalToProjectReqDto {
    private Long personalDocumentSeq;    // 이동할 개인 공유문서 ID
    private Long personalDriveChannelSeq; // 개인 드라이브 채널 ID
    private Long projectDriveChannelSeq; // 이동될 프로젝트 드라이브 채널 ID
    private String newDocumentName;      // 새 문서 이름 (선택사항, 없으면 원본 이름 사용)
}

