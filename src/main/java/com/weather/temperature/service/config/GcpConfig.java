package com.weather.temperature.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcpConfig {

    @Value("${GCP_TEMPERATURES_BUCKET_NAME}")
    private String bucketName;

    @Value("${GCP_TEMPERATURES_FILE_NAME}")
    private String fileName;

    @Value("${GCP_TEMPERATURES_PROJECT}")
    private String projectId;

    @Value("${GCP_TEMPERATURES_SUBSCRIPTION}")
    private String pubsubSubscription;

    public String getBucketName() {
        return bucketName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getPubsubSubscription() {
        return pubsubSubscription;
    }
}
