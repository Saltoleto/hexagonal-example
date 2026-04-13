-- Criação da tabela de saldos
-- Execute este script no MySQL antes de iniciar a aplicação

CREATE DATABASE IF NOT EXISTS saldos_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE saldos_db;

CREATE TABLE IF NOT EXISTS saldos (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    conta_id            VARCHAR(50)     NOT NULL,
    valor               DECIMAL(18, 2)  NOT NULL,
    moeda               VARCHAR(3)      NOT NULL,
    tipo                VARCHAR(20)     NOT NULL,
    data_referencia     DATETIME,
    data_processamento  DATETIME        NOT NULL,

    CONSTRAINT pk_saldos PRIMARY KEY (id),
    INDEX idx_saldos_conta_id (conta_id),
    INDEX idx_saldos_data_referencia (data_referencia),
    INDEX idx_saldos_tipo (tipo)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
