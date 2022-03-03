export SRVSYFOALTINN_USERNAME=$(cat /secrets/serviceuser/username)
export SRVSYFOALTINN_PASSWORD=$(cat /secrets/serviceuser/password)

export SPRING_DATASOURCE_USERNAME=$(cat /secrets/sykepengesoknad-altinn/credentials/username)
export SPRING_DATASOURCE_PASSWORD=$(cat /secrets/sykepengesoknad-altinn/credentials/password)
export SPRING_DATASOURCE_URL=$(cat /secrets/sykepengesoknad-altinn/config/jdbc_url)
