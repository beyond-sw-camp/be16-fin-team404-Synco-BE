package com.team404.synco.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drive")
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<?> getDriveHealthCheck() {
        return ResponseEntity.status(HttpStatus.OK).body("ok");
    }
}
