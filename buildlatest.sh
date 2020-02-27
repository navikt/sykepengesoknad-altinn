echo "Bygger syfoaltinn latest for docker compose utvikling"

mvn clean install -D skipTests

docker build . -t syfoaltinn:latest
