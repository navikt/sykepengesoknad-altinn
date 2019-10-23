CREATE TABLE SENDT_SOKNAD (
  ID          VARCHAR2(100) NOT NULL PRIMARY KEY,
  RESSURS_ID  VARCHAR2(100) NOT NULL,
  ALTINN_ID   VARCHAR2(100) NOT NULL,
  SENDT       TIMESTAMP     NOT NULL
);

CREATE SEQUENCE SENDT_SOKNAD_ID_SEQ
  START WITH 1;

CREATE INDEX RESSURSID_INDEX
  ON SENDT_SOKNAD (RESSURS_ID);
