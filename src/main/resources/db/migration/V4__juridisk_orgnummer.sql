CREATE TABLE JURIDISK_ORGNUMMER
(
    ID                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    ORGNUMMER          VARCHAR UNIQUE,
    JURIDISK_ORGNUMMER VARCHAR
);
