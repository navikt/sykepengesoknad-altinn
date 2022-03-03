FROM navikt/java:17
COPY init.sh /init-scripts/init.sh
COPY build/libs/app.jar /app/

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote"
