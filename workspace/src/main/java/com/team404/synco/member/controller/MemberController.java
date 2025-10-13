package com.team404.synco.member.controller;

import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.member.dto.*;
import com.team404.synco.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto<?>> createMember(@ModelAttribute @Validated CreateMemberDto createMemberDto) {
        Long memberSeq = memberService.createMemberWithValidation(createMemberDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ok(memberSeq, HttpStatus.CREATED));
    }

    @PostMapping("/doLogin")
    public ResponseEntity<ResponseDto<?>> doLogin(@RequestBody LoginReqDto loginReqDto) {
        LoginResDto loginResDto = memberService.doLogin(loginReqDto);
        return ResponseEntity.ok(ResponseDto.ok(loginResDto, HttpStatus.OK));
    }

    @GetMapping("/myPage")
    public ResponseEntity<ResponseDto<?>> getMyPage(@RequestHeader("X-Member-Seq") Long memberSeq) {
        return ResponseEntity.ok(ResponseDto.ok(memberService.myInfo(memberSeq), HttpStatus.OK));
    }

    @PatchMapping("/update")
    public ResponseEntity<ResponseDto<?>> updateMember(@RequestHeader("X-Member-Seq") Long memberSeq,
                                                       @ModelAttribute @Validated MemberUpdateDto dto) {
        MemberResDto memberResDto = memberService.updateMember(memberSeq, dto);
        return ResponseEntity.ok(ResponseDto.ok(memberResDto, HttpStatus.OK));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto<?>> deleteMember(@RequestHeader("X-Member-Seq") Long memberSeq) {
        memberService.deleteMemberYn(memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("OK", HttpStatus.OK));
    }

    @PostMapping("/refreshAt")
    public ResponseEntity<ResponseDto<?>> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto) {
        LoginResDto loginResDto = memberService.generateNewAt(refreshTokenDto);
        return ResponseEntity.ok(ResponseDto.ok(loginResDto, HttpStatus.OK));
    }

    @PostMapping("/findId")
    public ResponseEntity<ResponseDto<?>> findMemberId(@RequestBody @Validated FindIdReqDto findIdReqDto) {
        FindIdResDto findIdResDto = memberService.findMemberId(findIdReqDto);
        return ResponseEntity.ok(ResponseDto.ok(findIdResDto, HttpStatus.OK));
    }

    @PostMapping("/findPassword")
    public ResponseEntity<ResponseDto<?>> findPassword(@RequestBody @Validated FindPasswordReqDto findPasswordReqDto) {
        memberService.findPassword(findPasswordReqDto);
        return ResponseEntity.ok(ResponseDto.ok("임시 비밀번호가 이메일로 발송되었습니다.", HttpStatus.OK));
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<ResponseDto<?>> changePassword(@RequestHeader("X-Member-Seq") Long memberSeq,
                                                         @RequestBody @Validated ChangePasswordReqDto changePasswordReqDto) {
        memberService.changePassword(memberSeq, changePasswordReqDto);
        return ResponseEntity.ok(ResponseDto.ok("비밀번호가 성공적으로 변경되었습니다.", HttpStatus.OK));
    }

}
