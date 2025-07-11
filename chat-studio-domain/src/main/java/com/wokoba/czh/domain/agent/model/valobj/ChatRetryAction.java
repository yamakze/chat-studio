package com.wokoba.czh.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatRetryAction {
    NONE(0, "none"), // 默认无操作
    RETRY_ASSISTANT_RESPONSE(1, "retryAssistant"), // 移除上一次AI回复并重新回答
    REEDIT_USER_QUESTION(2, "reeditQuestion"); // 移除上一次用户问题与AI回复，重新提问

    private final Integer code;
    private final String label;

    public static ChatRetryAction fromCode(Integer code) {
        if (code == null) return NONE;
        for (ChatRetryAction action : values()) {
            if (action.code.equals(code)) return action;
        }
        return NONE;
    }
}
