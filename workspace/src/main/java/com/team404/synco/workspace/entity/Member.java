package com.team404.synco.workspace.entity;

import com.team404.synco.common.constant.ActiveStatus;
import com.team404.synco.common.constant.FriendStatus;
import com.team404.synco.common.constant.SocialType;
import com.team404.synco.common.constant.YnColumn;
import jakarta.persistence.*;
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
    @Column(nullable = false)
    private String memberId;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    @Builder.Default
    private ActiveStatus activeStatus = ActiveStatus.LOGOUT;
    private String statusMessage;
    private String profileImageUrl;
    @Column(columnDefinition = "CHAR(13)")
    private String telNo;
    private LocalDate birthDate;
    private SocialType socialType;
    private String socialId;
    @Column(nullable = false)
    @Builder.Default
    private String ynDel = YnColumn.IS_FALSE;
    @Column(nullable = false)
    @Builder.Default
    private String ynAlarmOffSet = YnColumn.IS_FALSE;
    @OneToMany(mappedBy = "alram")
    private List<Alarm> alarmList = new ArrayList<>();
    @OneToMany(mappedBy = "friend")
    private List<Friend> friendList = new ArrayList<>();
    @OneToMany(mappedBy = "workspace")
    private List<WorkSpace> workSpaceList = new ArrayList<>();
}
