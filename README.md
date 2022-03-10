# sykepengesoknad-altinn

## Om sykepengesoknad-altinn
Sykepengesoknad-altinn er ansvarlig for å oppdatere altinn med relevant informasjon etter innsending av søknader som skal sendes til arbeidsgiveren. 
Appen lytter på søknader som er sendt arbeidsgiver på topicet `flex.sykepengesoknad`. 
Om søknaden sendes til arbeidsgiveren styres av feltet `sendtArbeidsgiver` på kafka meldingen.

## Data
Applikasjonen har en database i GCP. 
Her lagres id på søknad som er sendt til altinn samt tidspunktet dette skjedde. 
Formålet med databasen er å ikke sende samme søknad flere ganger. 
Det slettes ikke data fra denne tabellen.

Applikasjonen har også en Google Cloud Storage Bucket. 
Denne brukes til å lagre en kopi av melding som sendes til altinn. 
Dette inkludere selve requesten til altinn, med søknaden strukturert som XML og en PDF versjon av søknaden. Dataene er personidentifiserbare. 
Dataene lagres med 5 års retention. Det er umulig å slette dataene før det har gått 5 år. Da slettes dataene automatisk med en lifecycle condition i bucketen.
Dette gjøres for å oppfylle etterlevelseskrav 54.


# Komme i gang

Bygges med gradle. Standard spring boot oppsett.

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles til flex@nav.no

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #flex.
