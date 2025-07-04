package com.wokoba.czh.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatRetryActionType {
    NONE(0, "none"), // 默认无特殊操作
    RETRY_LAST_ASSISTANT_RESPONSE(1, "retryAssistant"), // 重新回答：移除上一次AI的回复
    REEDIT_LAST_USER_QUESTION(2, "retryQuestion") // 重新编辑问题：移除上一次用户提问和AI的回复，然后新的问题会被追加进来
    ;
    private final Integer code;
    private final String info;
}