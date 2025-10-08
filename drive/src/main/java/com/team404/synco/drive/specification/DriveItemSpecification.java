package com.team404.synco.drive.specification;

import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.Folder;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class DriveItemSpecification {

    // 폴더 기본 조건 (드라이브 채널, 부모 폴더)
    public static Specification<Folder> folderByDriveChannelAndParent(Long driveChannelSeq, Long parentFolderSeq) {
        return (root, query, criteriaBuilder) -> {
            Predicate driveChannelPredicate = criteriaBuilder.equal(root.get("driveChannel").get("driveChannelSeq"), driveChannelSeq);
            Predicate parentFolderPredicate = criteriaBuilder.equal(root.get("parentFolderSeq"), parentFolderSeq);
            return criteriaBuilder.and(driveChannelPredicate, parentFolderPredicate);
        };
    }

    // 문서 기본 조건 (드라이브 채널, 부모 폴더)
    public static Specification<Document> documentByDriveChannelAndParent(Long driveChannelSeq, Long parentFolderSeq) {
        return (root, query, criteriaBuilder) -> {
            Join<Document, Folder> folderJoin = root.join("folder", JoinType.INNER);
            Predicate driveChannelPredicate = criteriaBuilder.equal(folderJoin.get("driveChannel").get("driveChannelSeq"), driveChannelSeq);
            Predicate parentFolderPredicate = criteriaBuilder.equal(folderJoin.get("parentFolderSeq"), parentFolderSeq);
            return criteriaBuilder.and(driveChannelPredicate, parentFolderPredicate);
        };
    }
}
