package com.team404.synco.search.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<?> getSearchHealthCheck() {
        return ResponseEntity.status(HttpStatus.OK).body("ok");
    }
}

