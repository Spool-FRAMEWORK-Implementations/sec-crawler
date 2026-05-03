FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl wget

WORKDIR /app

RUN addgroup -S spool && adduser -S crawler -G spool
USER crawler

COPY target/sec-crawler.jar app.jar

ENV SERVICE_NAME=sec-crawler
ENV OTEL_LOGS_ENDPOINT=http://host.docker.internal:3100/otlp/v1/logs
ENV OTEL_TRACES_ENDPOINT=http://host.docker.internal:4318/v1/traces
ENV OTEL_METRICS_ENDPOINT=http://host.docker.internal:4320/v1/metrics

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/spool/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]