package com.example.demo;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import io.opentelemetry.api.trace.Tracer;

@Configuration
public class OpenTelemetryConfig {

    @Value("${otel.exporter.otlp.application}")
    private String otlpExporterApplication;
    @Value("${otel.exporter.otlp.endpoint}")
    private String otlpExporterEndpoint;

    @Bean
    public OpenTelemetrySdk openTelemetry() {
        // Create a Resource with the service name attribute
        Resource resource = Resource.builder()
                .put("service.name", otlpExporterApplication)
                .build();

        OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpExporterEndpoint)
                .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(otlpExporter).build())
                .setResource(resource)
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();
    }

    @Bean
    public Tracer tracer(OpenTelemetrySdk openTelemetrySdk) {
        return openTelemetrySdk.getTracer(otlpExporterApplication);
    }
}
