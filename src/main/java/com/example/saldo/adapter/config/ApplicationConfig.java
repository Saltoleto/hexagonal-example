package com.example.saldo.adapter.config;

import com.example.saldo.core.port.in.BuscarSaldoUseCase;
import com.example.saldo.core.port.in.ProcessarSaldoUseCase;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import com.example.saldo.core.usecase.BuscarSaldoUseCaseImpl;
import com.example.saldo.core.usecase.ProcessarSaldoUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cola entre o core (POJOs puros) e o Spring.
 * Único lugar que conhece tanto as implementações do core quanto o container.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public ProcessarSaldoUseCase processarSaldoUseCase(SaldoRepositoryPort saldoRepositoryPort) {
        return new ProcessarSaldoUseCaseImpl(saldoRepositoryPort);
    }

    @Bean
    public BuscarSaldoUseCase buscarSaldoUseCase(SaldoRepositoryPort saldoRepositoryPort) {
        return new BuscarSaldoUseCaseImpl(saldoRepositoryPort);
    }
}
