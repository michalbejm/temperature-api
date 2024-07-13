package com.weather.temperature.service.config;

import org.springframework.beans.factory.annotation.Value;

public class GcpConfig {

    @Value("${GCP_TEMPERATURES_BUCKET_NAME}")
    private String bucketName;

    @Value("${GCP_TEMPERATURES_FILE_NAME}")
    private String fileName;

    public String getBucketName() {
        return bucketName;
    }

    public String getFileName() {
        return fileName;
    }
}
