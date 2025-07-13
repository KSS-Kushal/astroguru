package com.kss.astrologer.services.aws;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file, String location);
    void deleteFileByUrl(String fileUrl);
}
