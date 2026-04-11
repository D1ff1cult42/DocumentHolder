package com.d1ff.minio.service;

import com.d1ff.exceptions.MinioException;
import com.d1ff.exceptions.NotAllowedFileException;
import com.d1ff.minio.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "minio", name = "endpoint")
public class MinioService {
    private final MinioProperties minioProperties;

    private final MinioClient minioClient;

    private final MinioClient presignedMinioClient;

    public MinioService(MinioProperties minioProperties,
                        MinioClient minioClient,
                        @Qualifier("presignedMinioClient") MinioClient presignedMinioClient) {
        this.minioProperties = minioProperties;
        this.minioClient = minioClient;
        this.presignedMinioClient = presignedMinioClient;
    }


    public String uploadFile(String bucket, String objectName, Duration expireTime, MultipartFile file) {
        if (!minioProperties.getAllowedFileTypes().contains(file.getContentType())) {
            throw new NotAllowedFileException("File type not allowed");
        }

        if(file.getSize() <= 0 || file.getSize() > minioProperties.getFileMaxSizeBytes()) {
            throw new NotAllowedFileException("File size not allowed");
        }

        try{
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );

            if(!exists){
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket)
                        .build());
                log.info("Bucket created: {}", bucket);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), (long) -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return getPresignedUrl(bucket, objectName, expireTime);
        }catch (Exception e){
            throw new MinioException("Failed to upload file to Minio: " + e.getMessage());
        }
    }

    public String getPresignedUrl(String bucket, String objectName, Duration expireTime) {
        try{
            String url = presignedMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry((int)expireTime.toSeconds(), TimeUnit.SECONDS)
                            .build()
            );
            return url;
        }catch (Exception e){
            throw new MinioException("Failed to getPresignedUrl from Minio: " + e.getMessage());
        }
    }
}
