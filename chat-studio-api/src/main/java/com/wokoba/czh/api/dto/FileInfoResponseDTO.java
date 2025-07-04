package com.wokoba.czh.api.dto;

import lombok.Data;

@Data
public class FileInfoResponseDTO {
    private String fileName;
    private String fileUrl;
    private long fileSize;
    private String fileType;
}
