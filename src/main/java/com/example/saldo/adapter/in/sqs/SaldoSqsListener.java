package com.example.saldo.adapter.in.sqs;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.model.TipoSaldo;
import com.example.saldo.core.port.in.ProcessarSaldoUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Adaptador de ENTRADA (driving adapter).
 * Escuta a fila SQS, converte o DTO de mensagem para entidade de domínio
 * e invoca a porta de entrada (caso de uso).
 *
 * Responsabilidades:
 *  - Receber a mensagem
 *  - Fazer o mapeamento DTO → Domínio
 *  - Delegar ao caso de uso
 *  - Tratar erros de conversão/negócio
 */
@Component
public class SaldoSqsListener {

    private static final Logger log = LoggerFactory.getLogger(SaldoSqsListener.class);

    private final ProcessarSaldoUseCase processarSaldoUseCase;

    public SaldoSqsListener(ProcessarSaldoUseCase processarSaldoUseCase) {
        this.processarSaldoUseCase = processarSaldoUseCase;
    }

    @SqsListener("${app.sqs.queue-name}")
    public void onMessage(@Payload SaldoMensagemDto mensagem) {
        log.info("Mensagem recebida da fila SQS. contaId={}, tipo={}", mensagem.getContaId(), mensagem.getTipo());

        try {
            Saldo saldo = toDomain(mensagem);
            processarSaldoUseCase.processar(saldo);
        } catch (IllegalArgumentException e) {
            log.error("Mensagem inválida descartada: {}", e.getMessage());
            // Em produção: enviar para DLQ ou registrar métrica
        } catch (Exception e) {
            log.error("Erro inesperado ao processar mensagem SQS: {}", e.getMessage(), e);
            throw e; // Re-lança para SQS retentar (se configurado)
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
