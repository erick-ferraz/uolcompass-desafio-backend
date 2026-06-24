CREATE TABLE tb_transferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(15, 2) NOT NULL,
    payer_id BIGINT NOT NULL,
    payee_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_payer FOREIGN KEY (payer_id) REFERENCES tb_wallets(id),
    CONSTRAINT fk_payee FOREIGN KEY (payee_id) REFERENCES tb_wallets(id)
);
