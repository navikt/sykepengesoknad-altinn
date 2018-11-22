# syfoaltinn

## Om syfoaltinn
Syfoaltinn er ansvarlig for å oppdatere altinn med relevant informasjon etter innsending av søknader og sykmeldinger.

## For å starte appen lokalt:
Applikasjonen startes i `TestApplication.class` Den må kjøre som en spring-boot applikasjon med local som profil.

## Database
Appen kjører med en lokal H2 in-memory database. Den spinnes opp som en del av applikasjonen og er 
også tilgjengelig i tester. Du kan logge inn og kjøre spørringer på:
`localhost/h2` med jdbc_url: `jdbc:h2:mem:testdb`