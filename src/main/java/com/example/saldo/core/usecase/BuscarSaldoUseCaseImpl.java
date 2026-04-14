package com.example.saldo.core.usecase;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.port.in.BuscarSaldoPort;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Caso de uso: buscar saldo.
 *
 * Implementa a porta de entrada BuscarSaldoPort — é aqui que
 * o contrato declarado no core ganha comportamento concreto.
 *
 * Responsabilidades:
 *  - Consultar saldos via porta de saída SaldoRepositoryPort
 *  - Lançar exceção de domínio quando o saldo não for encontrado
 *
 * POJO puro — sem anotações Spring. Registrado como Bean via ApplicationConfig.
 */
public class BuscarSaldoUseCaseImpl implements BuscarSaldoPort {

    private static final Logger log = LoggerFactory.getLogger(BuscarSaldoUseCaseImpl.class);

    private final SaldoRepositoryPort saldoRepositoryPort;

    public BuscarSaldoUseCaseImpl(SaldoRepositoryPort saldoRepositoryPort) {
        this.saldoRepositoryPort = saldoRepositoryPort;
    }

    @Override
    public Saldo buscarPorId(Long id) {
        log.info("Buscando saldo por id={}", id);
        return saldoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new SaldoNaoEncontradoException("Saldo não encontrado para id=" + id));
    }

    @Override
    public List<Saldo> listarPorContaId(String contaId) {
        log.info("Listando saldos para contaId={}", contaId);
        return saldoRepositoryPort.listarPorContaId(contaId);
    }
}
