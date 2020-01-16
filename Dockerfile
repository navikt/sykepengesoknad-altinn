FROM navikt/java:8
COPY init.sh /init-scripts/init.sh
COPY target/app.jar /app/

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote"
