# Build stage
FROM azul/zulu-openjdk:21 AS builder

WORKDIR /app
COPY ./ /app
RUN chmod +x ./gradlew
RUN ./gradlew bootjar

# Runtime stage
FROM azul/zulu-openjdk:21
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/service.jar
COPY lib/opentelemetry-javaagent.jar /opt/opentelemetry-javaagent.jar
COPY lib/pyroscope.jar /opt/pyroscope.jar
EXPOSE 8080

# Set environment variables for container awareness
ENV JAVA_OPTS="-server \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/dumps/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -javaagent:/opt/opentelemetry-javaagent.jar \
    -javaagent:/opt/pyroscope.jar"

# Start the application
CMD java ${JAVA_OPTS} -jar /app/service.jar --spring.config.location=classpath:/application.yml,/config/application.yml