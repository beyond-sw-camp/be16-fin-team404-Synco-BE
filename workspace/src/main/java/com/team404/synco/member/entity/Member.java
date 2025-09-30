package com.team404.synco.member.entity;

import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.common.constant.SocialType;
import com.team404.synco.common.constant.YnColumn;
import com.team404.synco.alarm.entity.Alarm;
import com.team404.synco.common.entity.BaseEntity;
import com.team404.synco.friend.entity.Friend;
import com.team404.synco.member.dto.MemberUpdateDto;
import com.team404.synco.workspace.entity.WorkSpace;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberSeq;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false, unique = true)
    private String memberId;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ActiveStatus activeStatus = ActiveStatus.LOGOUT;
    private String statusMessage;
    private String profileImageUrl;
    @Column(columnDefinition = "CHAR(13)")
    private String telNo;
    private LocalDate birthDate;
    @Enumerated(EnumType.STRING)
    private SocialType socialType;
    private String socialId;
    @Column(nullable = false)
    @Builder.Default
    private String ynDel = YnColumn.IS_FALSE;
    @Column(nullable = false)
    @Builder.Default
    private String ynAlarmOffSet = YnColumn.IS_FALSE;
    @OneToMany(mappedBy = "member")
    private List<Alarm> alarmList = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<Friend> friendList = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<WorkSpace> workSpaceList = new ArrayList<>();

    public Member updateMember(MemberUpdateDto memberUpdateDTO) {
        this.memberId = memberUpdateDTO.getId();
        this.name = memberUpdateDTO.getName();
        this.email = memberUpdateDTO.getEmail();
        this.statusMessage = memberUpdateDTO.getStatusMessage();
        this.profileImageUrl = memberUpdateDTO.getProfileImageUrl();
        this.telNo = memberUpdateDTO.getTelNo();

        return this;
    }

    public void deleteMember(String ynDel){
        this.ynDel = ynDel;
    }
}
