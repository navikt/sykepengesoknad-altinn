export SRVSYFOALTINN_USERNAME=$(cat /secrets/serviceuser/username)
export SRVSYFOALTINN_PASSWORD=$(cat /secrets/serviceuser/password)

export SPRING_DATASOURCE_USERNAME=$(cat /secrets/syfoaltinn/credentials/username)
export SPRING_DATASOURCE_PASSWORD=$(cat /secrets/syfoaltinn/credentials/password)
export SPRING_DATASOURCE_URL=$(cat /secrets/syfoaltinn/config/jdbc_url)
