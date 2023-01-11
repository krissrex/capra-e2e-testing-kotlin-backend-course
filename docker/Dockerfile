FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:fc9db671e88f11569c49f0665506a460a576340d480e5604779a1c404890603d

RUN apk add --update --no-cache bind-tools
RUN set -eux; \
    adduser -S app

EXPOSE 8080

# Adds openTelemetry metrics and tracing: https://aws-otel.github.io/docs/getting-started/java-sdk/trace-auto-instr
# REQUIRES YOU TO ADD AN OTEL COLLECTOR SIDECAR IN AWS CDK!
ADD https://github.com/aws-observability/aws-otel-java-instrumentation/releases/latest/download/aws-opentelemetry-agent.jar /aws-opentelemetry-agent.jar
RUN chmod 777 /aws-opentelemetry-agent.jar
ENV OTEL_TRACES_SAMPLER parentbased_traceidratio
ENV OTEL_TRACES_SAMPLER_ARG 0.1
ENV OTEL_METRICS_EXPORTER otlp
ENV OTEL_EXPORTER_OTLP_ENDPOINT http://127.0.0.1:4317
ENV OTEL_EXPORTER_OTLP_COMPRESSION gzip
ENV OTEL_METRIC_EXPORT_INTERVAL 15000

ARG service_name=ServiceName
ARG service_namespace=customer-or-service-namespace
ARG service_version=git_commit_hash
ENV OTEL_RESOURCE_ATTRIBUTES service.name=$service_name,service.namespace=$service_namespace,service.version=$service_version,service=$service_name,version=$service_version

USER app
WORKDIR /

COPY app.jar /app.jar
CMD ["java", "-javaagent:/aws-opentelemetry-agent.jar", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
