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

package software.amazon.awssdk.core.signer;

import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.http.SdkHttpFullRequest;

/**
 * Interface for the signer used for signing the requests. All SDK signer implementations will implement this interface.
 */
@SdkPublicApi
@FunctionalInterface
public interface Signer {
    /**
     * Method that takes in an request and returns a signed version of the request.
     *
     * @param request The request to sign
     * @param executionAttributes Contains the attributes required for signing the request
     * @return A signed version of the input request
     */
    SdkHttpFullRequest sign(SdkHttpFullRequest request, ExecutionAttributes executionAttributes);

}
