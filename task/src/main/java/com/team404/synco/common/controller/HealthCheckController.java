package com.team404.synco.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<?> getTaskHealthCheck() {
        return ResponseEntity.status(HttpStatus.OK).body("ok");
    }
}
