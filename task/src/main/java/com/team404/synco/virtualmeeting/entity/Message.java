package com.team404.synco.virtualmeeting.entity;

import com.team404.synco.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    /** PK: 단순 정수 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 보낸 사람(우리 서비스의 멤버 PK) */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /** 방 FK — 실제 FK를 거는 것이 정합성에 유리 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_seq", nullable = false)   // FK 제약 활성화
    private Room room;

    /** 텍스트 본문 — 빈문자열은 허용하더라도 null은 방지 권장 */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;
}
