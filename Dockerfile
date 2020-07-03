FROM java:8-jdk-alpine
COPY ./target/demo-0.0.1-SNAPSHOT.jar /home/peter/apps/app/
WORKDIR /home/peter/apps/app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "demo-0.0.1-SNAPSHOT.jar"]
