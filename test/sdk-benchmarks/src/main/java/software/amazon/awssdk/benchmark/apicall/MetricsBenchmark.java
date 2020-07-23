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

package software.amazon.awssdk.benchmark.apicall;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import software.amazon.awssdk.benchmark.utils.MockServer;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.client.builder.SdkClientBuilder;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.services.protocolrestjson.ProtocolRestJsonAsyncClient;
import software.amazon.awssdk.services.protocolrestjson.ProtocolRestJsonAsyncClientBuilder;
import software.amazon.awssdk.services.protocolrestjson.ProtocolRestJsonClient;
import software.amazon.awssdk.services.protocolrestjson.ProtocolRestJsonClientBuilder;

/**
 * Benchmarking comparing metrics-enabled versus metrics-disabled performance.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Threads(25)
public class MetricsBenchmark {
    private MockServer mockServer;
    private ProtocolRestJsonClient enabledMetricsSyncClient;
    private ProtocolRestJsonAsyncClient enabledMetricsAsyncClient;
    private ProtocolRestJsonClient disabledMetricsSyncClient;
    private ProtocolRestJsonAsyncClient disabledMetricsAsyncClient;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        mockServer = new MockServer();
        mockServer.start();
        enabledMetricsSyncClient = enableMetrics(syncClientBuilder()).build();
        enabledMetricsAsyncClient = enableMetrics(asyncClientBuilder()).build();
        disabledMetricsSyncClient = enableMetrics(syncClientBuilder()).build();
        disabledMetricsAsyncClient = enableMetrics(asyncClientBuilder()).build();
    }

    private <T extends SdkClientBuilder<T, ?>> T enableMetrics(T syncClientBuilder) {
        return syncClientBuilder.overrideConfiguration(c -> c.addMetricPublisher(new EnabledPublisher()));
    }

    private ProtocolRestJsonClientBuilder syncClientBuilder() {
        return ProtocolRestJsonClient.builder()
                                     .endpointOverride(mockServer.getHttpUri())
                                     .httpClientBuilder(ApacheHttpClient.builder());
    }

    private ProtocolRestJsonAsyncClientBuilder asyncClientBuilder() {
        return ProtocolRestJsonAsyncClient.builder()
                                          .endpointOverride(mockServer.getHttpUri())
                                          .httpClientBuilder(NettyNioAsyncHttpClient.builder());
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        mockServer.stop();
        enabledMetricsSyncClient.close();
        enabledMetricsAsyncClient.close();
        disabledMetricsSyncClient.close();
        disabledMetricsAsyncClient.close();
    }

    @Benchmark
    public void metricsEnabledSync() {
        enabledMetricsSyncClient.allTypes();
    }

    @Benchmark
    public void metricsEnabledAsync() {
        enabledMetricsAsyncClient.allTypes().join();
    }

    @Benchmark
    public void metricsEnabledSyncStreamingInput() {
        enabledMetricsSyncClient.streamingInputOperation(r -> {}, RequestBody.fromString(""));
    }

    @Benchmark
    public void metricsEnabledAsyncStreamingInput() {
        enabledMetricsAsyncClient.streamingInputOperation(r -> {}, AsyncRequestBody.fromString("")).join();
    }

    @Benchmark
    public void metricsEnabledSyncStreamingOutput() {
        enabledMetricsSyncClient.streamingOutputOperationAsBytes(r -> {});
    }

    @Benchmark
    public void metricsEnabledAsyncStreamingOutput() {
        enabledMetricsAsyncClient.streamingOutputOperation(r -> {}, AsyncResponseTransformer.toBytes()).join();
    }

    @Benchmark
    public void metricsDisabledAsync() {
        disabledMetricsAsyncClient.allTypes().join();
    }

    @Benchmark
    public void metricsDisabledSyncStreamingInput() {
        disabledMetricsSyncClient.streamingInputOperation(r -> {}, RequestBody.fromString(""));
    }

    @Benchmark
    public void metricsDisabledAsyncStreamingInput() {
        disabledMetricsAsyncClient.streamingInputOperation(r -> {}, AsyncRequestBody.fromString("")).join();
    }

    @Benchmark
    public void metricsDisabledSyncStreamingOutput() {
        disabledMetricsSyncClient.streamingOutputOperationAsBytes(r -> {});
    }

    @Benchmark
    public void metricsDisabledAsyncStreamingOutput() {
        disabledMetricsAsyncClient.streamingOutputOperation(r -> {}, AsyncResponseTransformer.toBytes()).join();
    }

    public static void main(String... args) throws Exception {
        Options opt = new OptionsBuilder()
            .include(MetricsBenchmark.class.getSimpleName())
            .build();
        new Runner(opt).run();
    }

    private static final class EnabledPublisher implements MetricPublisher {
        @Override
        public void publish(MetricCollection metricCollection) {
        }

        @Override
        public void close() {
        }
    }
}
