package com.team404.synco.task.controller;

import com.team404.synco.common.constant.dto.ResponseDto;
import com.team404.synco.task.dto.TaskChannelMemberCreateReqDto;
import com.team404.synco.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
public class TaskController {
    private final TaskService taskService;
    @PostMapping("/create")
    public ResponseDto createTask(@RequestBody TaskChannelMemberCreateReqDto taskChannelMemberCreateReqDto){
        Long id = taskService.createTaskChannel(taskChannelMemberCreateReqDto);
        return ResponseDto.ok(id, HttpStatus.OK);
    };
}
