package com.example.saldo.core.usecase;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.port.in.ProcessarSaldoUseCase;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.transaction.Transactional;

/**
 * Implementação do caso de uso.
 *
 * Sem @Service — é um POJO puro que não conhece nenhum framework.
 * O Spring só sabe desta classe através do @Bean em ApplicationConfig.
 *
 * Isso preserva a regra: camadas de domínio e aplicação são
 * independentes de infraestrutura (Spring, JPA, AWS, etc).
 */
public class ProcessarSaldoUseCaseImpl implements ProcessarSaldoUseCase {

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
