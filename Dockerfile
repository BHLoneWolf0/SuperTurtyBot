FROM gradle:7.4-jdk18 as builder
WORKDIR /home/gradle/source/

COPY settings.gradle ./
COPY build.gradle ./
COPY src/ src/
RUN gradle installDist
RUN ls -Rla
FROM openjdk:18
WORKDIR /opt/SuperTurtyBot/
COPY --from=builder /home/gradle/source/build/install/SuperTurtyBot/ ./
RUN gradlew shadowJar
CMD ["java", "-jar", "./build/libs/SuperTurtyBot-all.jar", "--nogui", "--env=/env/.env"]