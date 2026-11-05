# syntax=docker/dockerfile:1
# Builds and runs one Spring Boot service from its module directory (the build context),
# e.g. context ../AddTask. The image is layered so dependency layers are cached between builds.

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
# The poms select JDK 25 through maven-toolchains-plugin; register the image's JDK for it.
RUN mkdir -p /root/.m2 && printf '%s\n' \
    '<toolchains><toolchain><type>jdk</type>' \
    '<provides><version>25</version></provides>' \
    "<configuration><jdkHome>${JAVA_HOME}</jdkHome></configuration>" \
    '</toolchain></toolchains>' > /root/.m2/toolchains.xml
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2/repository mvn -B -q dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository mvn -B -q -DskipTests package \
    && cp target/*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:25-jre-noble
RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring
WORKDIR /application
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./
USER spring
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
# Readiness over plain bash: the JRE image ships no curl or wget.
HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/${SERVER_PORT:-8080} \
    && printf "GET /actuator/health/readiness HTTP/1.0\r\n\r\n" >&3 && grep -q "\"UP\"" <&3' || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
