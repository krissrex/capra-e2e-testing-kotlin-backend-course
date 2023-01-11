FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:fc9db671e88f11569c49f0665506a460a576340d480e5604779a1c404890603d

RUN apk add --update --no-cache bind-tools
RUN set -eux; \
    adduser -S app

EXPOSE 8080

USER app
WORKDIR /

COPY app.jar /app.jar
CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
