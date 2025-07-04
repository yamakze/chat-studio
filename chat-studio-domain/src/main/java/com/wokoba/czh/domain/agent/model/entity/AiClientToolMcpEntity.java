package com.wokoba.czh.domain.agent.model.entity;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientToolMcpEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * MCP名称
     */
    private String mcpName;

    /**
     * 传输类型(sse/stdio)
     */
    private String transportType;

    /**
     * 传输配置 - sse
     */
    private TransportConfigSse transportConfigSse;

    /**
     * 传输配置 - stdio
     */
    private TransportConfigStdio transportConfigStdio;

    /**
     * 请求超时时间(分钟)
     */
    private Integer requestTimeout;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransportConfigSse {

        private Map<String, SseConfig> sseConfigs = new HashMap<>();


        public String getDefaultBaseUri() {
            return sseConfigs.values().stream()
                    .findFirst()
                    .map(SseConfig::getBaseUri)
                    .orElse(null);
        }

        @JsonAnySetter
        public void addSseConfig(String key, SseConfig config) {
            sseConfigs.put(key, config);
        }

        @Data
        @NoArgsConstructor
        public static class SseConfig {
            private String baseUri;
        }
    }



    /**
     * "mcp-server-weixin": {
     * "command": "java",
     * "args": [
     * "-Dspring.ai.mcp.server.stdio=true",
     * "-jar",
     * "/Users/fuzhengwei/Applications/apache-maven-3.8.4/repository/cn/bugstack/mcp/mcp-server-weixin/1.0.0/mcp-server-weixin-1.0.0.jar"
     * ]
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransportConfigStdio {

        private Map<String, Stdio> stdio;

        @Data
        public static class Stdio {
            private String command;
            private List<String> args;
            private Map<String,String> evn;
        }
    }

}
