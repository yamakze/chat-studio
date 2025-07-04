package com.wokoba.czh.domain.agent.service.rag;

import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.valobj.GitRepoEntity;
import com.wokoba.czh.domain.agent.service.IAiRagService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AiRagService implements IAiRagService {
    @Autowired
    private TokenTextSplitter tokenTextSplitter;
    @Autowired
    private IChatRepository repository;
    @Autowired
    private PgVectorStore vectorStore;

    @Override
    public void storeRagOrder(String name, String tag, List<Resource> files) {
        for (Resource file : files) {
            try {
                List<Document> docs = tokenTextSplitter.apply(
                        new TikaDocumentReader(file).read());
                docs.forEach(doc -> {
                    doc.getMetadata().put("knowledge", tag);
                    doc.getMetadata().put("filename", file.getFilename());
                });
                vectorStore.accept(docs);
            } catch (Exception e) {
                log.warn("Failed to process file: {}", file.getFilename(), e);
                throw new RuntimeException("解析文件失败");
            }
        }
        repository.storeRagOrder(name, tag);
    }

    @SneakyThrows
    @Override
    public void storeGitRepo(GitRepoEntity gitRepoEntity) {
        String localPath = "./git-cloned-repo";
        cloneRepoToLocal(gitRepoEntity.getRepoUrl(), gitRepoEntity.getUserName(), gitRepoEntity.getToken(), localPath);
        String projectName = gitRepoEntity.getProjectName();

        try {
            List<Resource> repoResources = new ArrayList<>();
            Files.walkFileTree(Paths.get(localPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    log.info("{} 遍历解析路径，上传知识库:{}", projectName, file.getFileName());
                    repoResources.add(new PathResource(file));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.info("Failed to access file: {} - {}", file.toString(), exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

            this.storeRagOrder(projectName, gitRepoEntity.getRepoUrl(), repoResources);
        } finally {
            FileUtils.deleteDirectory(new File(localPath));
        }
    }

    @Override
    public int deleteRagOrder(Long ragId) {
        String tag = repository.queryRagKnowledgeTag(ragId);
        vectorStore.delete(String.format("knowledge == '%s'", tag));
        return repository.deleteRagOrder(ragId);
    }

    private void cloneRepoToLocal(String repoUrl, String userName, String token, String localPath) throws Exception {
        File gitFile = new File(localPath);
        FileUtils.deleteDirectory(gitFile);
        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(gitFile)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, token))
                .call()
                .close();
    }
}
