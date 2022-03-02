/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.services.s3.extensions;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.awssdk.testutils.service.S3BucketUtils.temporaryBucketName;

import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import software.amazon.awssdk.services.s3.S3IntegrationTestBase;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.testutils.RandomTempFile;

public class DefaultS3ExtensionIntegrationTest extends S3IntegrationTestBase {

    private static final String BUCKET = temporaryBucketName(DefaultS3ExtensionIntegrationTest.class);
    private static final String KEY = "some-key";

    private static File file;

    @BeforeClass
    public static void setupFixture() throws IOException {
        createBucket(BUCKET);
        file = new RandomTempFile(10_000);
        s3.putObject(PutObjectRequest.builder().bucket(BUCKET).key(KEY).build(), file.toPath());
    }

    @AfterClass
    public static void tearDownFixture() {
        deleteBucketAndAllContents(BUCKET);
        file.delete();
    }

    @Test
    public void bucketExists() {
        assertThat(s3.doesBucketExist(BUCKET)).isTrue();
    }

    @Test
    public void bucketDoesNotExist() {
        assertThat(s3.doesBucketExist(temporaryBucketName("noexist"))).isFalse();
    }

    @Test
    public void objectExists() {
        assertThat(s3.doesObjectExist(BUCKET, KEY)).isTrue();
    }

    @Test
    public void objectDoesNotExist() {
        assertThat(s3.doesObjectExist(BUCKET, "noexist")).isFalse();
    }
}
