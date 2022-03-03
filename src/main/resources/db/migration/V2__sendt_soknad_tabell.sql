CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE SENDT_SOKNAD
(
    ID                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    SYKEPENGESOKNAD_ID VARCHAR(100) NOT NULL UNIQUE,
    ALTINN_ID          VARCHAR(100) NOT NULL,
    ALTINN_ID_ETTERS   VARCHAR(100),
    SENDT              TIMESTAMP WITH TIME ZONE
);


