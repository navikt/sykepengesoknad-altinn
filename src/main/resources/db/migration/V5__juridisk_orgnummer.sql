DROP TABLE JURIDISK_ORGNUMMER;

CREATE TABLE JURIDISK_ORGNUMMER
(
    ID                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    SYKMELDING_ID      VARCHAR UNIQUE NOT NULL,
    ORGNUMMER          VARCHAR        NOT NULL,
    JURIDISK_ORGNUMMER VARCHAR        NULL
);
