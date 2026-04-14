package com.example.saldo.core.usecase;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.port.in.ProcessarSaldoEventoPort;
import com.example.saldo.core.port.in.ProcessarSaldoPort;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: processar saldo.
 *
 * Implementa duas portas de entrada:
 *
 *  - ProcessarSaldoPort       → contrato genérico de processamento
 *  - ProcessarSaldoEventoPort → contrato de acionamento via evento assíncrono
 *
 * Ambas delegam para o mesmo método interno, garantindo que a lógica
 * de negócio existe em um único lugar independente do mecanismo de entrada.
 *
 * POJO puro — sem anotações Spring. Registrado como Bean via ApplicationConfig.
 */
public class ProcessarSaldoUseCaseImpl
        implements ProcessarSaldoPort, ProcessarSaldoEventoPort {

    private static final Logger log = LoggerFactory.getLogger(ProcessarSaldoUseCaseImpl.class);

    private final SaldoRepositoryPort saldoRepositoryPort;

    public ProcessarSaldoUseCaseImpl(SaldoRepositoryPort saldoRepositoryPort) {
        this.saldoRepositoryPort = saldoRepositoryPort;
    }

    @Override
    @Transactional
    public Saldo processar(Saldo saldo) {
        log.info("Processando saldo: {}", saldo);

        if (!saldo.isValido()) {
            throw new IllegalArgumentException(
                "Saldo inválido: campos obrigatórios ausentes. contaId=" + saldo.getContaId()
            );
        }

        Saldo salvo = saldoRepositoryPort.salvar(saldo);
        log.info("Saldo persistido com sucesso. id={}, contaId={}", salvo.getId(), salvo.getContaId());
        return salvo;
    }

    @Override
    public void aoReceberSaldo(Saldo saldo) {
        // Delega ao método principal — a lógica vive em um único lugar
        processar(saldo);
    }
}
