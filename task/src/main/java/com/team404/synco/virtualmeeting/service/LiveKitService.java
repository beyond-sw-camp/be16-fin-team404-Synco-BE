package com.team404.synco.virtualmeeting.service;

import com.team404.synco.virtualmeeting.entity.Room;
import com.team404.synco.virtualmeeting.entity.Recording;
import com.team404.synco.virtualmeeting.entity.RoomParticipant;
import com.team404.synco.virtualmeeting.repository.RecordingRepository;
import com.team404.synco.virtualmeeting.repository.RoomParticipantRepository;
import com.team404.synco.virtualmeeting.repository.RoomRepository;
import io.livekit.server.*;
import jakarta.persistence.EntityNotFoundException;
import livekit.LivekitEgress;
import livekit.LivekitEgress.*;
import livekit.LivekitWebhook.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LiveKitService {

    private final RoomRepository roomRepository;
    private final RecordingRepository recordingRepository;
    private final RoomParticipantRepository roomParticipantRepository;

    public void handleWebhook(WebhookEvent event) {
        switch (event.getEvent()) {
            case "room_started" -> handleRoomStarted(event);
            case "room_finished" -> handleRoomFinished(event);
            case "participant_joined" -> handleParticipantJoined(event);
            case "participant_left" -> handleParticipantLeft(event);
            case "participant_connection_aborted" -> handleParticipantConnectionAborted(event);
            case "track_published" -> handleTrackPublished(event);
            case "track_unpublished" -> handleTrackUnpublished(event);
            case "egress_started" -> handleEgressStarted(event);
            case "egress_updated" -> handleEgressUpdated(event);
            case "egress_ended" -> handleEgressEnded(event);
            case "ingress_started" -> handleIngressStarted(event);
            case "ingress_ended" -> handleIngressEnded(event);
        }
    }

    private void handleRoomStarted(WebhookEvent event) {
        log.info("LiveKit-Webhook(room_started) - {}", event.getRoom());
    }


    private void handleRoomFinished(WebhookEvent event) {
        log.info("LiveKit_Webhook(room_finished) - {}", event.getRoom());

        Room room = roomRepository.findById(Long.valueOf(event.getRoom().getName())).orElseThrow(() -> new EntityNotFoundException("회의를 찾을 수 없습니다."));
        room.endRoom();
    }

    private void handleParticipantJoined(WebhookEvent event) {
        log.info("LiveKit_Webhook(participant_joined) - {}", event.getRoom());
        log.info("LiveKit_Webhook(participant_joined) - {}", event.getParticipant());
    }

    private void handleParticipantLeft(WebhookEvent event) {
        log.info("LiveKit_Webhook(participant_left) - {}", event.getRoom());
        log.info("LiveKit_Webhook(participant_left) - {}", event.getParticipant());
        
        // EGRESS (녹화기)는 identity가 "EG_"로 시작하므로 스킵
        if (event.getParticipant() != null && event.getParticipant().getIdentity() != null && event.getParticipant().getIdentity().startsWith("EG_")) {
            log.info("EGRESS 녹화기는 참가자가 아니므로 스킵: identity={}", event.getParticipant().getIdentity());
            return;
        }
        
        // room과 identity로 participant 찾기
        Long roomSeq = Long.valueOf(event.getRoom().getName());
        Long memberSeq = Long.valueOf(event.getParticipant().getIdentity());
        
        Room room = roomRepository.findById(roomSeq).orElse(null);
        if (room == null) {
            log.warn("⚠️ Room을 찾을 수 없음: roomSeq={}", roomSeq);
            return;
        }
        
        Optional<RoomParticipant> participantOpt = roomParticipantRepository.findByRoomAndVirtualMeetingChannelMember_MemberSeq(room, memberSeq);
        
        if (participantOpt.isPresent()) {
            participantOpt.get().leaveRoom();
            log.info("✅ 참가자 퇴장 처리: roomSeq={}, memberSeq={}", roomSeq, memberSeq);
        } else {
            log.warn("⚠️ 참가자 정보를 찾을 수 없음: roomSeq={}, memberSeq={}", roomSeq, memberSeq);
        }
    }

    private void handleParticipantConnectionAborted(WebhookEvent event) {
        log.info("LiveKit_Webhook(participant_connection_aborted) - {}", event.getRoom());
        log.info("LiveKit_Webhook(participant_connection_aborted) - {}", event.getParticipant());

        // EGRESS (녹화기)는 identity가 "EG_"로 시작하므로 스킵
        if (event.getParticipant() != null && event.getParticipant().getIdentity() != null && event.getParticipant().getIdentity().startsWith("EG_")) {
            log.info("EGRESS 녹화기는 참가자가 아니므로 스킵: identity={}", event.getParticipant().getIdentity());
            return;
        }

        // room과 identity로 participant 찾기
        Long roomSeq = Long.valueOf(event.getRoom().getName());
        Long memberSeq = Long.valueOf(event.getParticipant().getIdentity());
        
        Room room = roomRepository.findById(roomSeq).orElse(null);
        if (room == null) {
            log.warn("⚠️ Room을 찾을 수 없음: roomSeq={}", roomSeq);
            return;
        }
        
        Optional<RoomParticipant> participantOpt = roomParticipantRepository.findByRoomAndVirtualMeetingChannelMember_MemberSeq(room, memberSeq);
        
        if (participantOpt.isPresent()) {
            participantOpt.get().leaveRoom();
            log.info("✅ 참가자 연결 중단 처리: roomSeq={}, memberSeq={}", roomSeq, memberSeq);
        } else {
            log.warn("⚠️ 참가자 정보를 찾을 수 없음: roomSeq={}, memberSeq={}", roomSeq, memberSeq);
        }
    }

    private void handleTrackPublished(WebhookEvent event) {
        log.info("LiveKit_Webhook(track_published) - {}", event.getRoom());
        log.info("LiveKit_Webhook(track_published) - {}", event.getParticipant());
        log.info("LiveKit_Webhook(track_published) - {}", event.getTrack());
    }

    private void handleTrackUnpublished(WebhookEvent event) {
        log.info("LiveKit_Webhook(track_unpublished) - {}", event.getRoom());
        log.info("LiveKit_Webhook(track_unpublished) - {}", event.getParticipant());
        log.info("LiveKit_Webhook(track_unpublished) - {}", event.getTrack());
    }

    private void handleEgressStarted(WebhookEvent event) {
        log.info("LiveKit_Webhook(egress_started) - {}", event.getEgressInfo());
        
        String egressId = event.getEgressInfo().getEgressId();
        Long roomSeq = Long.valueOf(event.getEgressInfo().getRoomName());
        
        // Recording은 REST에서 이미 생성됨 - 웹훅에서는 확인만
        Optional<Recording> existingRecording = recordingRepository.findByEgressId(egressId);
        
        if (existingRecording.isPresent()) {
            log.info("✅ Recording 이미 존재 (REST에서 생성됨): roomSeq={}, egressId={}", roomSeq, egressId);
        } else {
            log.warn("⚠️ Recording이 존재하지 않음 - REST에서 생성되지 않았거나 웹훅이 먼저 도착함: roomSeq={}, egressId={}", roomSeq, egressId);
        }
    }

    private void handleEgressUpdated(WebhookEvent event) {
        log.info("LiveKit_Webhook(egress_updated) - {}", event.getEgressInfo());
    }

    private void handleEgressEnded(WebhookEvent event) {
        log.info("LiveKit_Webhook(egress_ended) - {}", event.getEgressInfo());

        String egressId = event.getEgressInfo().getEgressId();
        Recording recording = recordingRepository.findByEgressId(egressId)
                .orElseThrow(() -> new EntityNotFoundException("Recording을 찾을 수 없습니다: egressId=" + egressId));

        if (event.getEgressInfo().getStatus() != EgressStatus.EGRESS_COMPLETE) {
            log.warn("녹화가 정상 완료되지 않음: egressId={}, status={}", egressId, event.getEgressInfo().getStatus());
            recording.markAsEnded();
            return;
        }

        // 정상 완료 케이스
        LivekitEgress.FileInfo fileInfo = event.getEgressInfo().getFileResults(0); // index 0로 넘어오는 첫 파일
        recording.updateFromFileInfo(fileInfo);

        log.info("✅ Recording 완료 업데이트: roomSeq={}, egressId={}, filename={}",
                recording.getRoom().getRoomSeq(), egressId, fileInfo.getFilename());
    }

    private void handleIngressStarted(WebhookEvent event) {
        log.info("LiveKit_Webhook(ingress_started) - {}", event.getIngressInfo());
    }

    private void handleIngressEnded(WebhookEvent event) {
        log.info("LiveKit_Webhook(ingress_ended) - {}", event.getIngressInfo());
    }
}