package com.team404.synco.task.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.task.dto.request.CommentCreateReqDto;
import com.team404.synco.task.dto.request.CommentUpdateReqDto;
import com.team404.synco.task.dto.response.CommentResDto;
import com.team404.synco.task.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    
    private final CommentService commentService;
    
    // 댓글 생성
    @PostMapping("/task/{taskSeq}")
    public ResponseEntity<ResponseDto<?>> createComment(@RequestHeader("X-Member-Seq") Long memberSeq,
                                                        @PathVariable Long taskSeq,
                                                        @RequestBody CommentCreateReqDto commentCreateReqDto) {
        Long commentSeq = commentService.createComment(memberSeq, taskSeq, commentCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.ok(commentSeq, HttpStatus.CREATED));
    }
    
    // 댓글 수정
    @PatchMapping("/{commentSeq}")
    public ResponseEntity<ResponseDto<?>> updateComment(@RequestHeader("X-Member-Seq") Long memberSeq,
                                                       @PathVariable Long commentSeq,
                                                       @RequestBody CommentUpdateReqDto commentUpdateReqDto) {
        commentService.updateComment(memberSeq, commentSeq, commentUpdateReqDto);
        return ResponseEntity.ok(ResponseDto.ok("댓글이 성공적으로 수정되었습니다.", HttpStatus.OK));
    }
    
    // 댓글 페이징 조회 (일반 댓글 + 대댓글)
    @GetMapping("/task/{taskSeq}")
    public ResponseEntity<ResponseDto<?>> getCommentsByTaskSeq(@RequestHeader("X-Member-Seq") Long memberSeq,
                                                               @PathVariable Long taskSeq,
                                                               Pageable pageable) {
        Page<CommentResDto> comments = commentService.getCommentsByTaskSeq(memberSeq, taskSeq, pageable);
        return ResponseEntity.ok(ResponseDto.ok(comments, HttpStatus.OK));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentSeq}")
    public ResponseEntity<ResponseDto<?>> deleteComment(@RequestHeader("X-Member-Seq") Long memberSeq,
                                                       @PathVariable Long commentSeq) {
        commentService.deleteComment(memberSeq, commentSeq);
        return ResponseEntity.ok(ResponseDto.ok("댓글이 성공적으로 삭제되었습니다.", HttpStatus.OK));
    }
}
