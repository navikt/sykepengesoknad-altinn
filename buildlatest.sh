echo "Bygger syfoaltinn latest for docker compose utvikling"

rm -rf ./build
./gradlew bootJar
docker build -t syfoaltinn:latest .
