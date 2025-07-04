package com.wokoba.czh.trigger.http;

import com.wokoba.czh.api.dto.FileInfoResponseDTO;
import com.wokoba.czh.types.common.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/v1/file")
public class AiChatFileController {

    /**
     * 上传聊天文件
     */
    @PostMapping("{chatClientId}")
    public ResponseEntity<String> uploadChatFile(@PathVariable Long chatClientId,
                                                 @RequestParam("files") @NotEmpty List<MultipartFile> files) {
        try {
            String dirPath = Constants.MEDIA_DIR + "/" + chatClientId + "/";
            File dir = new File(dirPath);
            if (!dir.exists() && !dir.mkdirs()) {
                log.error("无法创建目录: {}", dirPath);
                return ResponseEntity.internalServerError().body("无法创建上传目录");
            }
            for (MultipartFile file : files) {
                File dest = new File(dir, Objects.requireNonNull(file.getOriginalFilename()));
                file.transferTo(dest);
            }
            return ResponseEntity.ok("上传成功");
        } catch (Exception e) {
            log.error("上传文件失败 clientId:{},fileName:{}",
                    chatClientId, files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.joining(",")), e);
            return ResponseEntity.badRequest().body("上传文件失败");
        }
    }

    /**
     * 删除聊天文件
     */
    @DeleteMapping("/{chatClientId}")
    public ResponseEntity<String> deleteChatFile(@PathVariable Long chatClientId,
                                                 @RequestParam("fileNames") @NotEmpty List<String> fileNames) {
        try {
            String dirPath = Constants.MEDIA_DIR + "/" + chatClientId + "/";
            for (String fileName : fileNames) {
                File dest = new File(dirPath, fileName);
                dest.delete();
            }
            return ResponseEntity.ok("删除文件成功");
        } catch (Exception e) {
            log.error("删除文件失败 clientId:{},fileUrls:{}", chatClientId, fileNames, e);
            return ResponseEntity.badRequest().body("删除文件失败");
        }
    }

    /**
     * 获取聊天文件列表
     */
    @GetMapping("/{chatClientId}")
    public ResponseEntity<List<FileInfoResponseDTO>> getChatFileList(@PathVariable Long chatClientId) {
        log.info("获取客户端文件列表 clientId:{}", chatClientId);
        String dirPath = Constants.MEDIA_DIR + "/" + chatClientId + "/";
        List<FileInfoResponseDTO> fileNames = FileUtils.listFiles(new File(dirPath), null, false)
                .stream()
                .map(file -> {
                    FileInfoResponseDTO info = new FileInfoResponseDTO();
                    info.setFileName(file.getName());
                    info.setFileSize(file.length());
                    info.setFileType(FilenameUtils.getExtension(file.getName()));
                    info.setFileUrl(dirPath + file.getName());
                    return info;
                })
                .toList();
        return ResponseEntity.ok(fileNames);
    }
}
