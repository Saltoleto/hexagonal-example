package com.example.saldo.adapter.in.sqs;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.model.TipoSaldo;
import com.example.saldo.core.port.in.ProcessarSaldoEventoPort;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Adaptador de ENTRADA — AWS SQS.
 *
 * Responsabilidades:
 *  1. Receber a mensagem da fila
 *  2. Deserializar e converter DTO → domínio
 *  3. Acionar o core via ProcessarSaldoEventoPort
 *
 * Conhece apenas ProcessarSaldoEventoPort — o contrato que o core
 * declarou para recebimento de eventos. Não sabe nada sobre a
 * implementação concreta nem sobre outros contratos do core.
 *
 * Se amanhã o SQS for substituído por RabbitMQ ou Kafka, basta
 * criar um novo listener que também injete ProcessarSaldoEventoPort
 * — o core não muda, o contrato não muda.
 */
@Component
public class SaldoSqsListener {

    private static final Logger log = LoggerFactory.getLogger(SaldoSqsListener.class);

    private final ProcessarSaldoEventoPort processarSaldoEventoPort;

    public SaldoSqsListener(ProcessarSaldoEventoPort processarSaldoEventoPort) {
        this.processarSaldoEventoPort = processarSaldoEventoPort;
    }

    @SqsListener("${app.sqs.queue-name}")
    public void onMessage(@Payload SaldoMensagemDto mensagem) {
        log.info("Mensagem recebida da fila SQS. contaId={}, tipo={}", mensagem.getContaId(), mensagem.getTipo());

        try {
            Saldo saldo = toDomain(mensagem);
            processarSaldoEventoPort.aoReceberSaldo(saldo);
        } catch (IllegalArgumentException e) {
            log.error("Mensagem inválida descartada: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao processar mensagem SQS: {}", e.getMessage(), e);
            throw e;
        }
    }

    private Saldo toDomain(SaldoMensagemDto dto) {
        TipoSaldo tipo;
        try {
            tipo = TipoSaldo.valueOf(dto.getTipo().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Tipo de saldo inválido: " + dto.getTipo());
        }

        return new Saldo(
            dto.getContaId(),
            dto.getValor(),
            dto.getMoeda(),
            tipo,
            dto.getDataReferencia()
        );
    }
}
