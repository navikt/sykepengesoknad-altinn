# syfospringboot-kickstarter

## Om syfogsak
syfospringboot-kickstarter er et prosjekt som innholder en default spring-bootapp slik de er i syfosfæren. 
Appen som ligger på master har ingen eksterne avhengigheter, men det finnes brancher for å dra inn forskjellig 
funksjonalitet som WS eller databaser.

## Bruk:
1. lag et nytt repo for appen du skal lage
2. klon ned innholdet i syfospringboot-kickstarter: `kommado`
3. oppdater pom.xml med riktig app-informasjon
4. gjør en stringreplace på $APPLIKASJONSNAVN$ med nytt navn
5. merge inn evt. brancher for funksjonalitet du trenger (DB, WS, Kafka osv.)

