package com.example.saldo.core.port.in;

import com.example.saldo.core.model.Saldo;

/**
 * Porta de ENTRADA para processamento de saldo.
 *
 * Define o contrato pelo qual o mundo externo aciona o core
 * para processar um saldo recebido — sem saber se veio de SQS,
 * RabbitMQ, HTTP ou qualquer outro mecanismo.
 *
 * Implementada por: ProcessarSaldoUseCaseImpl (core/usecase)
 * Chamada por:      SaldoSqsListener (adapter/in/sqs)
 */
public interface ProcessarSaldoPort {

    /**
     * Processa e persiste um saldo recebido.
     *
     * @param saldo entidade de domínio com os dados do saldo
     * @return saldo salvo com ID gerado
     */
    Saldo processar(Saldo saldo);
}
