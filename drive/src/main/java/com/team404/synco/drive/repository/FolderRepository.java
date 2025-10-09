package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.DriveChannel;
import com.team404.synco.drive.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long>, JpaSpecificationExecutor<Folder> {
    List<Folder> findByParentFolderSeq(Long parentFolderSeq);
    
    // 특정 드라이브 채널 내의 최상위 폴더들 조회 (parentFolderSeq가 null인 경우)
    @Query("SELECT MAX(f.orders) FROM Folder f WHERE f.parentFolderSeq = :parentFolderSeq AND f.driveChannel.driveChannelSeq = :driveChannelSeq")
    Optional<Long> findMaxOrdersByParentFolderSeqAndDriveChannelSeq(@Param("parentFolderSeq") Long parentFolderSeq, @Param("driveChannelSeq") Long driveChannelSeq);
    
    // 최상위 폴더들의 최대 orders 조회 (parentFolderSeq가 null인 경우)
    @Query("SELECT MAX(f.orders) FROM Folder f WHERE f.parentFolderSeq IS NULL AND f.driveChannel.driveChannelSeq = :driveChannelSeq")
    Optional<Long> findMaxOrdersByParentFolderSeqIsNullAndDriveChannelSeq(@Param("driveChannelSeq") Long driveChannelSeq);

    @Query("UPDATE Folder f SET f.orders = f.orders + 1 WHERE f.parentFolderSeq = :parentFolderSeq AND f.driveChannel.driveChannelSeq = :driveChannelSeq AND f.orders >= :fromOrder")
    @Modifying
    void incrementOrdersFrom(@Param("parentFolderSeq") Long parentFolderSeq, @Param("driveChannelSeq") Long driveChannelSeq, @Param("fromOrder") Long fromOrder);
    
    @Query("UPDATE Folder f SET f.orders = f.orders - 1 WHERE f.parentFolderSeq = :parentFolderSeq AND f.driveChannel.driveChannelSeq = :driveChannelSeq AND f.orders > :fromOrder")
    @Modifying
    void decrementOrdersFrom(@Param("parentFolderSeq") Long parentFolderSeq, @Param("driveChannelSeq") Long driveChannelSeq, @Param("fromOrder") Long fromOrder);

    // 폴더명 중복 체크
    Optional<Folder> findByFolderNameAndFolderSeqNotAndDriveChannelAndParentFolderSeq(String folderName, Long folderSeq, DriveChannel driveChannel, Long parentFolderSeq);
}
