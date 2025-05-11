/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.utils;

import java.net.URI;
import java.nio.file.Path;

import com.atlauncher.managers.LogManager;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3Utils {
    /**
     * Checks if the S3 credentials and endpoint are valid by attempting to access the bucket.
     *
     * @param endpoint        Custom endpoint URL (can be null for AWS)
     * @param region          AWS region
     * @param accessKey       AWS access key
     * @param secretAccessKey AWS secret key
     * @param bucketName      Bucket name to check
     * @return true if connection and access are valid, false otherwise
     */
    public static boolean checkConnection(String endpoint, String region, String accessKey, String secretAccessKey, String bucketName) {
        try (S3Client s3 = buildS3Client(endpoint, region, accessKey, secretAccessKey)) {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (S3Exception e) {
            LogManager.error("S3 Connection failed: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            LogManager.error("S3 Connection failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Uploads a file to S3
     *
     * @param endpoint        Custom endpoint URL (can be null for AWS)
     * @param region          AWS region
     * @param accessKey       AWS access key
     * @param secretAccessKey AWS secret access key
     * @param bucketName      Bucket name to upload to
     * @param path            Path within S3 to upload to including the filename
     * @param file            Path to the file to upload
     */
    public static void upload(String endpoint, String region, String accessKey, String secretAccessKey, String bucketName, String path, Path file) {
        try (S3Client s3 = buildS3Client(endpoint, region, accessKey, secretAccessKey)) {
            s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(path).build(), file);
        } catch (S3Exception e) {
            LogManager.error("S3 Upload failed: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            LogManager.error("S3 Upload failed: " + e.getMessage());
        }
    }

    private static S3Client buildS3Client(String endpoint, String region, String accessKey, String secretAccessKey) {
        S3Client s3;
        if (endpoint != null && !endpoint.isEmpty()) {
            s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretAccessKey)))
                .build();
        } else {
            s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretAccessKey)))
                .build();
        }
        return s3;
    }
}