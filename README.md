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

## Kafka
### Oppsett
- Man må også legge til miljøvariabler for `ssl.truststore`. For å få disse ressursene, spør en utvikler nær deg.
- Placeholder for topic name må byttes ut, find and replace `$topicName$`.
- Placeholder for topic id må byttes ut, find and replace `$topicId$`.
- Opprett topic, se under.

### Opprett topic
For å ta i bruk kafka trenger man en topic. Denne oppretes gjennom [kafka-adminrest](https://github.com/navikt/kafka-adminrest). Apiet er dokumentert med swagger: https://kafka-adminrest-q4.nais.preprod.local/api/v1.

Navngivingskonvenson for kafka topics finnes [her](https://confluence.adeo.no/display/SOAG/R2+-+Navngivning+av+Topic). TL;DR: navnet skal være i fortid og ha dette formatet: `aapen-syfo-<navn>-v1`.

Dette skal til for å lage ny topic:
1. `POST` til `/api/v1/topics` for å lage topic.
2. `PUT` til `/api/v1/topics/{topicName}/groups` for å sette gruppe for producer og listener.
3. `PUT` til `/api/v1/topics/{topicName}/groups` for å sette utviklere som managers for topicken.

Eksempel:
```json
{
  "type": "MANAGER",
  "name": "{topicName}",
  "members": [
    "CN=B151344,OU=Users,OU=NAV,OU=BusinessUnits,DC=adeo,DC=no",
    "CN=S149030,OU=Users,OU=NAV,OU=BusinessUnits,DC=adeo,DC=no",
    "CN=O142910,OU=Users,OU=NAV,OU=BusinessUnits,DC=adeo,DC=no",
    "CN=N143409,OU=Users,OU=NAV,OU=BusinessUnits,DC=adeo,DC=no"
  ],
  "ldapResult": {
    "resultCode": {
      "intValue": 0,
      "name": "success",
      "stringRepresentation": "0 (success)"
    },
    "message": ""
  }
}
```
