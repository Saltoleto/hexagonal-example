CREATE TABLE pedido (
    id          UUID        PRIMARY KEY,
    descricao   VARCHAR(255) NOT NULL,
    valor       NUMERIC(10,2) NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    cep         VARCHAR(9),
    logradouro  VARCHAR(255),
    cidade      VARCHAR(100),
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pedido_status ON pedido(status);
