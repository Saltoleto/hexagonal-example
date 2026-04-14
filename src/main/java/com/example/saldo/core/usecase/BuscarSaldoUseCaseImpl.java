package com.example.saldo.core.usecase;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.port.in.BuscarSaldoHttpPort;
import com.example.saldo.core.port.in.BuscarSaldoPort;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Caso de uso: buscar saldo.
 *
 * Implementa duas portas de entrada:
 *
 *  - BuscarSaldoPort     → contrato genérico de consulta
 *  - BuscarSaldoHttpPort → contrato de acionamento via requisição HTTP
 *
 * Ambas delegam para os mesmos métodos internos, garantindo que a lógica
 * de negócio existe em um único lugar independente do protocolo de entrada.
 *
 * POJO puro — sem anotações Spring. Registrado como Bean via ApplicationConfig.
 */
public class BuscarSaldoUseCaseImpl
        implements BuscarSaldoPort, BuscarSaldoHttpPort {

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
