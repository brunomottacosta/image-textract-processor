FROM openjdk:13-jdk-slim

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    APP_SLEEP=5 \
    JAVA_OPTS=""

CMD echo "The application will start in ${APP_SLEEP}s..." && \
    sleep ${APP_SLEEP} && \
    java ${JAVA_OPTS} -Dserver.port=8080 -Djava.security.egd=file:/dev/./urandom -jar /app.jar

EXPOSE 8080

ADD *.jar /app.jar
