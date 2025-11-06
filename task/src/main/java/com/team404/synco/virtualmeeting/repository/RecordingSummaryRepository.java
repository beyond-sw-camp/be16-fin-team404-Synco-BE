package com.team404.synco.virtualmeeting.repository;

import com.team404.synco.virtualmeeting.entity.RecordingSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordingSummaryRepository extends JpaRepository<RecordingSummary, Long> {
    Optional<RecordingSummary> findByRecording_RecordingSeq(Long recordingSeq);
}
