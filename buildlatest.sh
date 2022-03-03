echo "Bygger sykepengesoknad-altinn latest for docker compose utvikling"

rm -rf ./build
./gradlew bootJar
docker build -t sykepengesoknad-altinn:latest .
