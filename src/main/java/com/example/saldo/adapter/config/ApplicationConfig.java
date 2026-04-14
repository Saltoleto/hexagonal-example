package com.example.saldo.adapter.config;

import com.example.saldo.core.port.in.BuscarSaldoHttpPort;
import com.example.saldo.core.port.in.BuscarSaldoPort;
import com.example.saldo.core.port.in.ProcessarSaldoEventoPort;
import com.example.saldo.core.port.in.ProcessarSaldoPort;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import com.example.saldo.core.usecase.BuscarSaldoUseCaseImpl;
import com.example.saldo.core.usecase.ProcessarSaldoUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cola entre o core (POJOs puros) e o Spring.
 *
 * Instancia os casos de uso uma única vez e os expõe tipados
 * por cada porta que implementam. Os adapters recebem apenas
 * o contrato específico do seu mecanismo de entrada — nunca
 * a implementação concreta nem uma porta mais ampla do que precisam.
 *
 * ProcessarSaldoUseCaseImpl implementa:
 *   - ProcessarSaldoPort       → contrato genérico
 *   - ProcessarSaldoEventoPort → contrato de evento (usado pelo SQS listener)
 *
 * BuscarSaldoUseCaseImpl implementa:
 *   - BuscarSaldoPort     → contrato genérico
 *   - BuscarSaldoHttpPort → contrato HTTP (usado pelo REST controller)
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public ProcessarSaldoUseCaseImpl processarSaldoUseCase(SaldoRepositoryPort saldoRepositoryPort) {
        return new ProcessarSaldoUseCaseImpl(saldoRepositoryPort);
    }

    @Bean
    public BuscarSaldoUseCaseImpl buscarSaldoUseCase(SaldoRepositoryPort saldoRepositoryPort) {
        return new BuscarSaldoUseCaseImpl(saldoRepositoryPort);
    }
}
