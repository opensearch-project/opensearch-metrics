package org.opensearchmetrics.util;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class S3Util {
    private final S3Client s3Client;
    private final String bucketName;

    public S3Util(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public ResponseInputStream<GetObjectResponse> getObjectInputStream(String objectName) {
        try {
            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(objectName)
                    .bucket(bucketName)
                    .build();

            return s3Client.getObject(objectRequest);
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to get object from S3", e);
        }
    }

    public List<String> listObjectsKeys(String prefix) {
        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Iterable listRes = s3Client.listObjectsV2Paginator(listReq);

            return listRes.stream()
                    .flatMap(r -> r.contents().stream())
                    .map(S3Object::key)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (S3Exception e) {
            System.out.println(e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to list object keys from S3", e);
        }
    }
}
