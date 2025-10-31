package com.team404.synco.alarm.controller;

import com.team404.synco.alarm.dto.AlarmFindReqDto;
import com.team404.synco.alarm.dto.AlarmResDto;
import com.team404.synco.alarm.service.AlarmService;
import com.team404.synco.common.dto.ResponseDto;
import com.team404.synco.common.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RequestMapping("/alarms")
@RestController
@RequiredArgsConstructor
public class AlarmController {
    private final SseService sseService;
    private final AlarmService alarmService;

    // SSE 연결 엔드포인트
    @GetMapping("/sse/connect")
    public SseEmitter subscribe(@RequestHeader("X-Member-Seq")Long memberSeq) {
            return sseService.connect(memberSeq);
    }

    // 알림 전체 목록 조회
    @GetMapping("")
    public ResponseEntity<ResponseDto<?>> alarmList(@RequestHeader("X-Member-Seq")Long memberSeq) throws IOException {
        List<AlarmResDto> alarmResDtoList = alarmService.findMyAlarmList(memberSeq);
        return ResponseEntity.ok().body(ResponseDto.ok(alarmResDtoList, HttpStatus.OK));
    }

    // 알림 읽음 처리(단건)
    @PatchMapping("/{alarmSeq}")
    public ResponseEntity<ResponseDto<?>> readAlarm(@PathVariable("alarmSeq") Long alarmSeq) throws IOException {
        alarmService.readAlarm(alarmSeq);
        return ResponseEntity.ok().body(ResponseDto.ok("알림 읽음처리 성공", HttpStatus.OK));
    }

    // 알림 읽음 처리(개인 목록)
    @PatchMapping("/personal")
    public ResponseEntity<ResponseDto<?>> readAllPersonalAlarm(@RequestHeader("X-Member-Seq")Long memberSeq) throws IOException {
        alarmService.readAllPersonalAlarm(memberSeq);
        return ResponseEntity.ok().body(ResponseDto.ok("전체 알림 읽음처리 성공", HttpStatus.OK));
    }

    // 알림 읽음 처리(프로젝트 목록)
    @PatchMapping("/project")
    public ResponseEntity<ResponseDto<?>> readAllProjectAlarm(@RequestHeader("X-Member-Seq")Long memberSeq,
                                                              @RequestBody AlarmFindReqDto alarmFindReqDto) throws IOException {
        alarmService.readAllProjectAlarm(memberSeq, alarmFindReqDto);
        return ResponseEntity.ok().body(ResponseDto.ok("전체 알림 읽음처리 성공", HttpStatus.OK));
    }

    // 알림 읽음 처리(개인워크스페이스에서 타입별로)
    @PatchMapping("/personal/type")
    public ResponseEntity<ResponseDto<?>> readAllPersonalAlarmByType(@RequestHeader("X-Member-Seq")Long memberSeq,
                                                                     @RequestBody AlarmFindReqDto alarmFindReqDto) throws IOException {
        alarmService.readAllPersonalAlarmByType(memberSeq, alarmFindReqDto);
        return ResponseEntity.ok().body(ResponseDto.ok("전체 알림 읽음처리 성공", HttpStatus.OK));
    }

    // 알림 읽음 처리(프로젝트에서 타입별로)
    @PatchMapping("/project/type")
    public ResponseEntity<ResponseDto<?>> readAllProjectAlarmByType(@RequestHeader("X-Member-Seq")Long memberSeq,
                                                                    @RequestBody AlarmFindReqDto alarmFindReqDto) throws IOException {
        alarmService.readAllProjectAlarmByType(memberSeq, alarmFindReqDto);
        return ResponseEntity.ok().body(ResponseDto.ok("전체 알림 읽음처리 성공", HttpStatus.OK));
    }

    // 알림 삭제 처리(개인 목록)
    @DeleteMapping("/personal")
    public ResponseEntity<ResponseDto<?>> deleteAllPersonalAlarm(@RequestHeader("X-Member-Seq")Long memberSeq) throws IOException {
        alarmService.deleteAllPersonalAlarm(memberSeq);
        return ResponseEntity.ok().body(ResponseDto.ok("전체 알림 읽음처리 성공", HttpStatus.OK));
    }

    // 알림 삭제 처리(프로젝트 목록)
    @DeleteMapping("/project")
    public ResponseEntity<ResponseDto<?>> deleteAllProjectAlarm(@RequestHeader("X-Member-Seq")Long memberSeq,
                                                              @RequestBody AlarmFindReqDto alarmFindReqDto) throws IOException {
        alarmService.deleteAllProjectAlarm(memberSeq, alarmFindReqDto);
        return ResponseEntity.ok().body(ResponseDto.ok("전체 알림 읽음처리 성공", HttpStatus.OK));
    }

    // 알림 삭제 처리(단건)
    @DeleteMapping("/{alarmSeq}")
    public ResponseEntity<ResponseDto<?>> deleteAlarm(@PathVariable("alarmSeq") Long alarmSeq) throws IOException {
        alarmService.deleteAlarm(alarmSeq);
        return ResponseEntity.ok().body(ResponseDto.ok("알림 삭제 성공", HttpStatus.OK));
    }

    // 알림 삭제 처리(타입별)
    @DeleteMapping("/personal/type")
    public ResponseEntity<ResponseDto<?>> deleteAllPersonalAlarmByType(@RequestHeader("X-Member-Seq")Long memberSeq,
                                                                       @RequestBody AlarmFindReqDto alarmFindReqDto) throws IOException {
        alarmService.deleteAllPersonalAlarmByType(memberSeq, alarmFindReqDto);
        return ResponseEntity.ok().body(ResponseDto.ok("타입별 알림 삭제 성공", HttpStatus.OK));
    }

    // 알림 읽음 처리(프로젝트에서 타입별로)
    @DeleteMapping("/project/type")
    public ResponseEntity<ResponseDto<?>> deleteAllProjectAlarmByType(@RequestHeader("X-Member-Seq")Long memberSeq,
                                                                      @RequestBody AlarmFindReqDto alarmFindReqDto) throws IOException {
        alarmService.deleteAllProjectAlarmByType(memberSeq, alarmFindReqDto);
        return ResponseEntity.ok().body(ResponseDto.ok("전체 알림 읽음처리 성공", HttpStatus.OK));
    }
}
