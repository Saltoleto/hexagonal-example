package com.example.saldo.core.port.in;

import com.example.saldo.core.model.Saldo;

/**
 * Porta de ENTRADA (driving port).
 * Define o contrato que o adaptador de entrada (SQS) deve chamar.
 * O domínio expõe o que pode fazer; quem chama não conhece a implementação.
 */
public interface ProcessarSaldoUseCase {

    /**
     * Processa e persiste um saldo recebido.
     *
     * @param saldo entidade de domínio com os dados de saldo
     * @return saldo salvo com ID gerado
     */
    Saldo processar(Saldo saldo);
}
