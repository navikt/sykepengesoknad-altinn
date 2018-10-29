# syfospringboot-kickstarter

## Om syfospringboot-kickstarter
syfospringboot-kickstarter er et prosjekt som innholder en default spring-bootapp slik de er i syfosfæren. 
Appen som ligger på master har ingen eksterne avhengigheter, men det finnes brancher for å dra inn forskjellig 
funksjonalitet som WS eller databaser.

## Bruk:
1. lag et nytt _TOMT_ repo for appen du skal lage på github uten license og .gitignore
2. klon ned innholdet i syfospringboot-kickstarter:
    - `git clone --bare git@github.com:navikt/syfospringboot-kickstarter.git temp`
    - `cd temp`
    - `git push --mirror git push --mirror git@github.com:navikt/$APPLIKASJONSNAVN$.git`
    - Fjern temp repo: `rm -rf temp.git`
3. oppdater pom.xml, README.md med riktig app-informasjon
4. gjør en stringreplace på $APPLIKASJONSNAVN$ med nytt navn
5. merge inn evt. brancher for funksjonalitet du trenger (DB, WS, Kafka osv.)

## Tips og triks:
En kan plukke commits fra kickstarter til prosjektene som arver fra den. Kommandoen er: `git fetch git@github.com:navikt/syfospringboot-kickstarter.git <branch> && git cherry-pick <commit_hash>
`

## Database
Appen kjører med en lokal H2 in-memory database. Den spinnes opp som en del av applikasjonen og er 
også tilgjengelig i tester. Du kan logge inn og kjøre spørringer på:
`localhost/h2` med jdbc_url: `jdbc:h2:mem:testdb`