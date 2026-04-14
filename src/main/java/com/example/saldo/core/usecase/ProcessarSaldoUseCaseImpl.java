package com.example.saldo.core.usecase;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.port.in.ProcessarSaldoPort;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: processar saldo.
 *
 * Implementa a porta de entrada ProcessarSaldoPort — é aqui que
 * o contrato declarado no core ganha comportamento concreto.
 *
 * Responsabilidades:
 *  - Aplicar a regra de validação do domínio
 *  - Delegar a persistência à porta de saída SaldoRepositoryPort
 *
 * POJO puro — sem anotações Spring. Registrado como Bean via ApplicationConfig.
 */
public class ProcessarSaldoUseCaseImpl implements ProcessarSaldoPort {

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
}
