package com.wokoba.czh.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),
    MISS_CLIENT_MATERIALS("0003", "客户端缺少必要的物料"),
    MODEL_SUPPLIER_EXCEPTION("0004", "模型供应商提供模型异常"),
    AI_MODEL_MISSING("0005", "模型数据缺失"),
    ;

    private String code;
    private String info;

}
