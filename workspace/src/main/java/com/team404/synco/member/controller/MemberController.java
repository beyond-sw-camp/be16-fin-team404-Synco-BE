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
        Long id = memberService.save(createMemberDto);
        return new ResponseEntity<>(ResponseDto.ok(id, HttpStatus.CREATED)
                , HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody LoginReqDto loginReqDto) {
        Member member = memberService.doLogin(loginReqDto);
        String accessToken = jwtTokenProvider.createAtToken(member);
        String refreshToken = jwtTokenProvider.createRtToken(member);
        
        LoginResDto loginResDto = LoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return new ResponseEntity<>(ResponseDto.ok(loginResDto, HttpStatus.OK), HttpStatus.OK);

    }

    @GetMapping("/myPage")
    public ResponseEntity<?> myPage(@RequestHeader("X-User-Id")Long id) {
        return new ResponseEntity<>(ResponseDto.ok(memberService.myInfo(id), HttpStatus.OK), HttpStatus.OK);
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateMyPage(@RequestHeader("X-User-Id") Long memberId,
                                          @Valid @RequestBody MemberUpdateDto dto) {
        MemberResDto result = memberService.updateMember(memberId, dto);
        return new ResponseEntity<>(ResponseDto.ok(result, HttpStatus.OK), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestHeader("X-User-Id")Long id){
        memberService.delete(id);
        return new ResponseEntity<>(ResponseDto.ok("OK", HttpStatus.OK), HttpStatus.OK);
    }

    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto) {
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());
        String accessToken = jwtTokenProvider.createAtToken(member);

        LoginResDto loginResDto = LoginResDto.builder()
                .accessToken(accessToken)
                .build();

        return new ResponseEntity<>(ResponseDto.ok(loginResDto, HttpStatus.OK), HttpStatus.OK);
    }


    @GetMapping("/check-member-id")
    public ResponseEntity<?> checkMemberId(@RequestHeader("X-User-Id") String userId) {
        System.out.println("API Gateway로부터 전달받은 X-User-Id 헤더: " + userId);

        return new ResponseEntity<>(ResponseDto.ok("전달받은 memberId는 " + userId + " 입니다.", HttpStatus.OK), HttpStatus.OK);
    }


}
