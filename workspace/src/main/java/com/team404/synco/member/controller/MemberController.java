package com.team404.synco.member.controller;

import com.team404.synco.common.auth.JwtTokenProvider;
import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.member.dto.*;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseDto<?> memberCreate(@ModelAttribute @Validated CreateMemberDto createMemberDto) {
        Long memberSeq = memberService.createMemberWithValidation(createMemberDto);
        return ResponseDto.ok(memberSeq, HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseDto<?> doLogin(@RequestBody LoginReqDto loginReqDto) {
        LoginResDto loginResDto = memberService.doLogin(loginReqDto);
        return ResponseDto.ok(loginResDto, HttpStatus.OK);
    }

    @GetMapping("/myPage")
    public ResponseDto<?> myPage(@RequestHeader("X-Member-Seq") Long memberSeq) {
        return ResponseDto.ok(memberService.myInfo(memberSeq), HttpStatus.OK);
    }

    @PatchMapping("/update")
    public ResponseDto<?> updateMyPage(@RequestHeader("X-Member-Seq") Long memberSeq,
                                          @ModelAttribute @Validated MemberUpdateDto dto) {
        MemberResDto memberResDto = memberService.updateMember(memberSeq, dto);
        return ResponseDto.ok(memberResDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseDto<?> memberDelete(@RequestHeader("X-Member-Seq") Long memberSeq){
        memberService.memberDeleteYn(memberSeq);
        return ResponseDto.ok("OK", HttpStatus.OK);
    }

    @PostMapping("/refreshAt")
    public ResponseDto<?> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto) {
        LoginResDto loginResDto = memberService.generateNewAt(refreshTokenDto);
        return ResponseDto.ok(loginResDto, HttpStatus.OK);
    }

}
