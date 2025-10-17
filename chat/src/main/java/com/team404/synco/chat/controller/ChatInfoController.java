package com.team404.synco.chat.controller;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat-info")
public class ChatInfoController implements ApplicationListener<WebServerInitializedEvent> {

    private int port;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
    }

    @GetMapping("/port")
    public int getPort() {
        return port;
    }
}
