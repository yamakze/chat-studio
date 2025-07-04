package com.wokoba.czh.domain.agent.service;


public interface IAiAgentPreheatService {

    /**
     * 服务预热，启动时触达
     */
    void preheat() throws Exception;

    void preheat(Long ...aiClientIds) throws Exception;

}
