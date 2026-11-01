FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B clean verify -Djavafx.platform=linux

FROM eclipse-temurin:17-jdk-jammy

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

RUN apt-get update && apt-get install -y \
    fontconfig \
    fonts-dejavu-core \
    fonts-noto-core \
    fonts-noto-cjk \
    libgl1 \
    libglx0 \
    libgtk-3-0 \
    libxcursor1 \
    libxext6 \
    libxfixes3 \
    libxi6 \
    libxinerama1 \
    libxrandr2 \
    libxrender1 \
    libxtst6 \
    libxxf86vm1 \
    libx11-6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/OTP1_inclass1_assignment-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]
