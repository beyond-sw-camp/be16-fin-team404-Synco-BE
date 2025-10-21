package com.team404.synco.virtualmeeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveKitWebhookDto {
    
    @JsonProperty("event")
    private String event;
    
    @JsonProperty("room")
    private RoomInfo room;
    
    @JsonProperty("participant")
    private ParticipantInfo participant;
    
    @JsonProperty("track")
    private TrackInfo track;
    
    @JsonProperty("recording")
    private RecordingInfo recording;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomInfo {
        @JsonProperty("sid")
        private String sid;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("metadata")
        private String metadata;
        
        @JsonProperty("num_participants")
        private Integer numParticipants;
        
        @JsonProperty("creation_time")
        private Long creationTime;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        @JsonProperty("sid")
        private String sid;
        
        @JsonProperty("identity")
        private String identity;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("metadata")
        private String metadata;
        
        @JsonProperty("joined_at")
        private Long joinedAt;
        
        @JsonProperty("left_at")
        private Long leftAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackInfo {
        @JsonProperty("sid")
        private String sid;
        
        @JsonProperty("type")
        private String type; // "audio", "video", "screen"
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("muted")
        private Boolean muted;
        
        @JsonProperty("enabled")
        private Boolean enabled;
        
        @JsonProperty("published_at")
        private Long publishedAt;
        
        @JsonProperty("unpublished_at")
        private Long unpublishedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordingInfo {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("status")
        private String status; // "started", "finished", "failed"
        
        @JsonProperty("started_at")
        private Long startedAt;
        
        @JsonProperty("finished_at")
        private Long finishedAt;
        
        @JsonProperty("duration")
        private Long duration;
        
        @JsonProperty("url")
        private String url;
    }
}
