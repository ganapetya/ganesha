FROM java:8-jdk-alpine
ENV CURRENT_DIR .
WORKDIR /app
COPY $CURRENT_DIR/target/demo-0.0.1-SNAPSHOT.jar /app
COPY $CURRENT_DIR/start.sh /app
EXPOSE 8081
EXPOSE 5005
RUN ["chmod", "+x", "./start.sh"]
ENTRYPOINT ["./start.sh"]
