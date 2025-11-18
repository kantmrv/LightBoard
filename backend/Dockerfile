FROM gradle:8.5-jdk21 AS gradle-base

WORKDIR /app

COPY build.gradle .
COPY settings.gradle .

RUN gradle dependencies --no-daemon

COPY src src

EXPOSE 8080

CMD ["gradle", "bootRun", "--no-daemon"]
