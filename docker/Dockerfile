# todo-list service: runs the jar that `mvn package` in the repository root writes to docker/
# (spring-boot-maven-plugin <outputDirectory>docker</outputDirectory>, <finalName>todo-list</finalName>).
FROM eclipse-temurin:25-jre-noble
RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring
WORKDIR /app
COPY --chown=spring:spring todo-list.jar todo-list.jar
USER spring
EXPOSE 8081
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/${SERVER_PORT:-8081} \
    && printf "GET /actuator/health/readiness HTTP/1.0\r\n\r\n" >&3 && grep -q "\"UP\"" <&3' || exit 1
ENTRYPOINT ["java", "-jar", "/app/todo-list.jar"]
