package com.example.saldo.core.port.in;

import com.example.saldo.core.model.Saldo;

/**
 * Porta de entrada para recebimento de saldo via evento assíncrono.
 *
 * Declara formalmente que o core aceita acionamentos via mensageria,
 * sem saber se o mecanismo é SQS, RabbitMQ, Kafka ou qualquer outro.
 *
 * Implementada por: ProcessarSaldoUseCaseImpl (core/usecase)
 * Chamada por:      SaldoSqsListener (adapter/in/sqs)
 */
public interface ProcessarSaldoEventoPort {

    /**
     * Aciona o processamento de um saldo recebido via evento.
     *
     * @param saldo entidade de domínio já convertida pelo adapter
     */
    void aoReceberSaldo(Saldo saldo);
}
