package com.team404.synco.member.controller;

import com.team404.synco.common.auth.JwtTokenProvider;
import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.member.dto.*;
import com.team404.synco.member.entity.Member;
import com.team404.synco.member.service.MemberService;
import jakarta.validation.Valid;
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
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<?> create(@ModelAttribute @Validated CreateMemberDto createMemberDto) {
        Long memberSeq = memberService.createMemberWithValidation(createMemberDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(memberSeq, HttpStatus.CREATED));
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody LoginReqDto loginReqDto) {
        LoginResDto loginResDto = memberService.doLogin(loginReqDto);
        return ResponseEntity.ok(ResponseDto.ok(loginResDto, HttpStatus.OK));
    }

    @GetMapping("/myPage")
    public ResponseEntity<?> myPage(@RequestHeader("X-Member-Seq") Long memberSeq) {
        return ResponseEntity.ok(ResponseDto.ok(memberService.myInfo(memberSeq), HttpStatus.OK));
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateMyPage(@RequestHeader("X-Member-Seq") Long memberSeq,
                                          @ModelAttribute @Validated MemberUpdateDto dto) {
        MemberResDto result = memberService.updateMember(memberSeq, dto);
        return ResponseEntity.ok(ResponseDto.ok(result, HttpStatus.OK));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestHeader("X-Member-Seq") Long memberSeq){
        memberService.memberDeleteYn(memberSeq);
        return ResponseEntity.ok(ResponseDto.ok("OK", HttpStatus.OK));
    }

    @PostMapping("/refreshAt")
    public ResponseEntity<?> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto) {
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());
        String accessToken = jwtTokenProvider.createAtToken(member);

        LoginResDto loginResDto = LoginResDto.builder()
                .accessToken(accessToken)
                .build();

        return ResponseEntity.ok(ResponseDto.ok(loginResDto, HttpStatus.OK));
    }

    @GetMapping("/checkMemberId")
    public ResponseEntity<?> checkMemberId(@RequestHeader("X-Member-Seq") Long memberSeq) {
        String message = memberService.checkMemberId(memberSeq);
        return ResponseEntity
                .ok(ResponseDto.ok(message, HttpStatus.OK));
    }

}
