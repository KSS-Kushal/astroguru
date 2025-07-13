package com.kss.astrologer.services.aws;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kss.astrologer.exceptions.CustomException;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3ServiceImpl implements S3Service {

    private final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private final String bucketName = dotenv.get("S3_BUCKET_NAME");

    private final String s3BaseUrl = dotenv.get("S3_ENDPOINT_URL");

    @Autowired
    private S3Client s3Client;

    @Override
    public String uploadFile(MultipartFile file, String location) {
        String fileName = file.getOriginalFilename();
        String extension = "";

        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        String fileKey = location + "/" + UUID.randomUUID() + "." + extension;
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
//                    .acl(ObjectCannedACL.PUBLIC_READ) // Publicly accessible
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Image Upload Failed");
        }

        return s3BaseUrl + "/" + fileKey;
    }

    @Override
    public void deleteFileByUrl(String fileUrl) {
        // Extract the key from the full URL
        String fileKey = fileUrl.replace(s3BaseUrl + "/", "");

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}
