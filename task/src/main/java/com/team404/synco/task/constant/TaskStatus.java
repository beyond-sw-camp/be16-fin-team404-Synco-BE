package com.team404.synco.common.constant;

import lombok.Getter;

@Getter
public enum TaskStatus {
    TODO("할 일"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료");

    private final String displayName;

    TaskStatus(final String displayName) {
        this.displayName = displayName;
    }
}
