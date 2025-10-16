package com.team404.synco.drive.dto;

import com.team404.synco.drive.entity.DocumentMessageMethod;
import com.team404.synco.drive.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UpdateDocumentReqDto {

    // 바꾸는 부분의 문서 변경과 관련된 정보(create, update, delete 공통)
    private Long documentLineSeq; // 문서 변경 시퀀스
    private String parentDocumentLineSeq; // 문서 변경 부모 시퀀스(create인 경우에는 null)
    private String documentFeId; // 문서 변경 feid

    // 바뀌는 부분의 문서 라인과 관련된 정보(create, change 공통)
    private Long documentChangeLineSeq; // 바뀌는 부분의 문서 라인 시
    private String parentDocumentChangeLineSeq;// 바뀌는 부분의 부모 문서 라인 시
    private String documentChangeFeId; // 바뀌는 부분의 문서 라인 feid

    private Long documentId; // 문서 아이디
    private DocumentMessageMethod method; // 문서 변경 메서드
    private String content; // 바뀌는 부분의 문서 라인 내용
    private Type blockType; // 바뀌는 부분의 문서 라인 타입
    @Builder.Default
    private Integer blockLevel = 0; //front의 h태그 기능을 위해 추가
    @Builder.Default
    private Integer blockIndent = 0; //front의 tap 기능을 위해 추가
}
