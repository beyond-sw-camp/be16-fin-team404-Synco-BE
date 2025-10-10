package com.team404.synco.drive.repository;

import com.team404.synco.drive.entity.DriveChannel;
import com.team404.synco.drive.entity.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByParentFolderSeq(Long parentFolderSeq);
    
    // 특정 드라이브 채널 내의 최상위 폴더들 조회 (parentFolderSeq가 null인 경우)
    @Query("SELECT MAX(f.orders) FROM Folder f WHERE f.parentFolderSeq = :parentFolderSeq AND f.driveChannel.driveChannelSeq = :driveChannelSeq")
    Optional<Long> findMaxOrdersByParentFolderSeqAndDriveChannelSeq(@Param("parentFolderSeq") Long parentFolderSeq, @Param("driveChannelSeq") Long driveChannelSeq);
    
    // 최상위 폴더들의 최대 orders 조회 (parentFolderSeq가 null인 경우)
    @Query("SELECT MAX(f.orders) FROM Folder f WHERE f.parentFolderSeq IS NULL AND f.driveChannel.driveChannelSeq = :driveChannelSeq")
    Optional<Long> findMaxOrdersByParentFolderSeqIsNullAndDriveChannelSeq(@Param("driveChannelSeq") Long driveChannelSeq);

    // orders 조정 메서드 (범위 지정) - 순서 변경용
    @Query("UPDATE Folder f SET f.orders = f.orders + 1 WHERE f.parentFolderSeq = :parentFolderSeq AND f.driveChannel.driveChannelSeq = :driveChannelSeq AND f.orders >= :fromOrder AND f.orders <= :toOrder")
    @Modifying
    void incrementOrdersInRange(@Param("parentFolderSeq") Long parentFolderSeq, @Param("driveChannelSeq") Long driveChannelSeq, @Param("fromOrder") Long fromOrder, @Param("toOrder") Long toOrder);

    // orders 조정 메서드 (범위 지정) - 순서 변경용
    @Query("UPDATE Folder f SET f.orders = f.orders - 1 WHERE f.parentFolderSeq = :parentFolderSeq AND f.driveChannel.driveChannelSeq = :driveChannelSeq AND f.orders >= :fromOrder AND f.orders <= :toOrder")
    @Modifying
    void decrementOrdersInRange(@Param("parentFolderSeq") Long parentFolderSeq, @Param("driveChannelSeq") Long driveChannelSeq, @Param("fromOrder") Long fromOrder, @Param("toOrder") Long toOrder);

    // orders 조정 메서드 (폴더 이동용)
    @Query("UPDATE Folder f SET f.orders = f.orders + 1 WHERE f.parentFolderSeq = :parentFolderSeq AND f.driveChannel.driveChannelSeq = :driveChannelSeq AND f.orders >= :fromOrder")
    @Modifying
    void incrementOrdersFrom(@Param("parentFolderSeq") Long parentFolderSeq, @Param("driveChannelSeq") Long driveChannelSeq, @Param("fromOrder") Long fromOrder);

    // orders 조정 메서드 (폴더 이동용)
    @Query("UPDATE Folder f SET f.orders = f.orders - 1 WHERE f.parentFolderSeq = :parentFolderSeq AND f.driveChannel.driveChannelSeq = :driveChannelSeq AND f.orders > :fromOrder")
    @Modifying
    void decrementOrdersFrom(@Param("parentFolderSeq") Long parentFolderSeq, @Param("driveChannelSeq") Long driveChannelSeq, @Param("fromOrder") Long fromOrder);
    
    // 최상위 폴더들의 orders 조정 메서드 (parentFolderSeq가 null인 경우)
    @Query("UPDATE Folder f SET f.orders = f.orders - 1 WHERE f.parentFolderSeq IS NULL AND f.driveChannel.driveChannelSeq = :driveChannelSeq AND f.orders > :fromOrder")
    @Modifying
    void decrementOrdersFromTopLevel(@Param("driveChannelSeq") Long driveChannelSeq, @Param("fromOrder") Long fromOrder);

    // 폴더명 중복 체크
    Optional<Folder> findByFolderNameAndFolderSeqNotAndDriveChannelAndParentFolderSeq(String folderName, Long folderSeq, DriveChannel driveChannel, Long parentFolderSeq);
    
    // 드라이브 채널과 폴더 ID로 폴더 검색
    Optional<Folder> findByFolderSeqAndDriveChannelDriveChannelSeq(Long folderSeq, Long driveChannelSeq);
    
    // 특정 부모 폴더 하위의 모든 폴더 조회 (순서대로)
    List<Folder> findByParentFolderSeqAndDriveChannelDriveChannelSeqOrderByOrders(Long parentFolderSeq, Long driveChannelSeq);
    
    // 최상위 폴더들 조회 (순서대로)
    List<Folder> findByParentFolderSeqIsNullAndDriveChannelDriveChannelSeqOrderByOrders(Long driveChannelSeq);
    
    // 페이징 쿼리: 특정 드라이브 채널과 부모 폴더의 폴더들 조회
    @Query("SELECT f FROM Folder f WHERE f.driveChannel.driveChannelSeq = :driveChannelSeq AND " +
           "(:parentFolderSeq IS NULL AND f.parentFolderSeq IS NULL OR f.parentFolderSeq = :parentFolderSeq)")
    Page<Folder> findFoldersByDriveChannelAndParent(@Param("driveChannelSeq") Long driveChannelSeq, 
                                                   @Param("parentFolderSeq") Long parentFolderSeq, 
                                                   Pageable pageable);
}
