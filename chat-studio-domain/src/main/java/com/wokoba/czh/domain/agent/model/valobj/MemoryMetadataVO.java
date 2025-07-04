package com.wokoba.czh.domain.agent.model.valobj;

public record MemoryMetadataVO() {
    // ---- 元数据字段名 ----
    public static final String CLIENT_ID = "clientId";
    public static final String MEMORY_TYPE = "memoryType";
    public static final String TIMESTAMP = "timestamp";
    public static final String IMPORT_ANCE_SCORE = "importanceScore";

    // ---- 记忆类型 ----
    public static final String TYPE_CONSOLIDATED = "CONSOLIDATED_FACT";
    public static final String TYPE_SYNTHESIS = "SYNTHESIS";
    public static final String TYPE_CONTRADICTION = "CONTRADICTION";

}