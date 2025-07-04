package com.wokoba.czh.domain.agent.service.media;

import com.wokoba.czh.domain.agent.service.AttachmentProcessor;
import com.wokoba.czh.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AttachmentService implements AttachmentProcessor {

    private final ResourceLoader resourceLoader;

    @Autowired
    public AttachmentService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public UserMessage handleChatAttachments(String userText, String filePattern, Long clientId) {
        if (userText == null) {
            return UserMessage.builder().build();
        }
        processedText processedText = extractMatches(userText, filePattern);
        List<Media> mediaList = getMediaByNames(clientId, processedText.fileNames);

        return UserMessage.builder()
                .media(mediaList)
                .text(processedText.text)
                .build();
    }


    private List<Media> getMediaByNames(Long clientId, List<String> fileNames) {
        Assert.notNull(clientId, "clientId不能为null!");
        return fileNames.stream()
                .map(fileName -> createMedia(String.format("%s/%d/%s", Constants.MEDIA_DIR, clientId, fileName)))
                .filter(Objects::nonNull)
                .toList();
    }

    private Media createMedia(String path) {
        Resource resource = resourceLoader.getResource("file:" + path);

        if (!resource.exists()) {
            log.warn("文件不存在: {}", path);
            return null;
        }

        return Media.builder()
                .data(resource)
                .name(resource.getFilename())
                .mimeType(MimeTypeUtils.parseMimeType(
                        Objects.requireNonNull(URLConnection.guessContentTypeFromName(resource.getFilename()))))
                .build();
    }


    private processedText extractMatches(String content, String filePattern) {
        List<String> matches = new ArrayList<>();
        StringBuilder newText = new StringBuilder();

        try {
            Pattern regex = Pattern.compile(filePattern);
            Matcher matcher = regex.matcher(content);

            int lastEnd = 0;
            while (matcher.find()) {
                String fileName = matcher.group(1);
                if (fileName != null && !fileName.isEmpty()) {
                    matches.add(fileName);
                    // 将匹配的部分替换为文件名
                    newText.append(content, lastEnd, matcher.start());
                    newText.append(fileName);
                    lastEnd = matcher.end();
                }
            }
            // 添加剩余的文本
            newText.append(content.substring(lastEnd));
        } catch (Exception e) {
            log.error("正则表达式匹配失败: {}", content, e);
        }

        // 返回替换后的新文本
        return new processedText(newText.toString(), matches);
    }

    record processedText(String text, List<String> fileNames) {

    }
}
