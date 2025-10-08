package com.team404.synco.drive.specification;

import com.team404.synco.drive.entity.Document;
import com.team404.synco.drive.entity.Folder;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriveItemSpecification {

    // 폴더 필터링
    public static Specification<Folder> filterFolder(Map<String, Object> filterKey) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            for (String key : filterKey.keySet()) {
                Object value = filterKey.get(key);
                if (value != null) {
                    if (key.equals("driveChannelSeq")) {
                        predicates.add(criteriaBuilder.equal(root.get("driveChannel").get("driveChannelSeq"), value));
                    } else if (key.equals("parentFolderSeq")) {
                        predicates.add(criteriaBuilder.equal(root.get("parentFolderSeq"), value));
                    } else if (key.equals("nameFilter")) {
                        predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("folderName")), 
                            "%" + value.toString().toLowerCase() + "%"
                        ));
                    } else if (key.equals("dateFilter")) {
                        // 날짜 필터링 로직
                        predicates.add(criteriaBuilder.equal(root.get("createdAt"), value));
                    }
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // 문서 필터링
    public static Specification<Document> filterDocument(Map<String, Object> filterKey) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            for (String key : filterKey.keySet()) {
                Object value = filterKey.get(key);
                if (value != null) {
                    if (key.equals("driveChannelSeq")) {
                        Join<Document, Folder> folderJoin = root.join("folder", JoinType.INNER);
                        predicates.add(criteriaBuilder.equal(folderJoin.get("driveChannel").get("driveChannelSeq"), value));
                    } else if (key.equals("parentFolderSeq")) {
                        Join<Document, Folder> folderJoin = root.join("folder", JoinType.INNER);
                        predicates.add(criteriaBuilder.equal(folderJoin.get("parentFolderSeq"), value));
                    } else if (key.equals("nameFilter")) {
                        predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("documentName")), 
                            "%" + value.toString().toLowerCase() + "%"
                        ));
                    } else if (key.equals("dateFilter")) {
                        predicates.add(criteriaBuilder.equal(root.get("createdAt"), value));
                    } else if (key.equals("sizeFilter")) {
                        // 크기 필터링 로직 (Integer로 변환)
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            criteriaBuilder.length(root.get("documentUrl")), 
                            ((Long) value).intValue()
                        ));
                    }
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
