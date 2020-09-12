FROM java:8-jdk-alpine
ENV CURRENT_DIR .
WORKDIR /app
COPY $CURRENT_DIR/target/demo-0.0.1-SNAPSHOT.jar /app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "demo-0.0.1-SNAPSHOT.jar"]
