package com.wokoba.czh.domain.agent.service;

import com.wokoba.czh.domain.agent.model.valobj.GitRepoEntity;
import org.springframework.core.io.Resource;

import java.util.List;


public interface IAiRagService {

    void storeRagOrder(String name, String tag, List<Resource> files);

    void storeGitRepo(GitRepoEntity gitRepoEntity);

    int deleteRagOrder(Long ragId);
}
