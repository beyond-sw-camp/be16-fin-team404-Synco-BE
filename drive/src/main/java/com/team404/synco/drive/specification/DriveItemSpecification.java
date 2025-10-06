package com.team404.synco.drive.specification;

import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.Folder;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class DriveItemSpecification {

    // 폴더 정렬을 위한 Specification
    public static Specification<Folder> folderSort(String sortBy, String sortOrder) {
        return (root, query, criteriaBuilder) -> {
            if ("name".equals(sortBy)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(criteriaBuilder.desc(root.get("folderName")));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get("folderName")));
                }
            } else if ("date".equals(sortBy)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get("createdAt")));
                }
            }
            return null;
        };
    }

    // 문서 정렬을 위한 Specification
    public static Specification<Document> documentSort(String sortBy, String sortOrder) {
        return (root, query, criteriaBuilder) -> {
            if ("name".equals(sortBy)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(criteriaBuilder.desc(root.get("documentName")));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get("documentName")));
                }
            } else if ("date".equals(sortBy)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get("createdAt")));
                }
            } else if ("size".equals(sortBy)) {
                if ("desc".equals(sortOrder)) {
                    query.orderBy(criteriaBuilder.desc(root.get("documentUrl")));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get("documentUrl")));
                }
            }
            return null;
        };
    }

    // 폴더 필터링
    public static Specification<Folder> folderFilter(Long driveChannelSeq, Long parentFolderSeq) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            
            if (driveChannelSeq != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(root.get("driveChannel").get("driveChannelSeq"), driveChannelSeq));
            }
            
            if (parentFolderSeq != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(root.get("parentFolderSeq"), parentFolderSeq));
            }
            
            return predicate;
        };
    }

    // 문서 필터링
    public static Specification<Document> documentFilter(Long driveChannelSeq, Long parentFolderSeq) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            
            if (driveChannelSeq != null) {
                Join<Document, Folder> folderJoin = root.join("folder", JoinType.INNER);
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(folderJoin.get("driveChannel").get("driveChannelSeq"), driveChannelSeq));
            }
            
            if (parentFolderSeq != null) {
                Join<Document, Folder> folderJoin = root.join("folder", JoinType.INNER);
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(folderJoin.get("parentFolderSeq"), parentFolderSeq));
            }
            
            return predicate;
        };
    }
}
