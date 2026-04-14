package com.example.saldo.adapter.config;

import com.example.saldo.core.port.in.BuscarSaldoPort;
import com.example.saldo.core.port.in.ProcessarSaldoPort;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import com.example.saldo.core.usecase.BuscarSaldoUseCaseImpl;
import com.example.saldo.core.usecase.ProcessarSaldoUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cola entre o core (POJOs puros) e o Spring.
 *
 * Instancia os casos de uso e os registra como Beans tipados
 * pelas suas portas de entrada — garantindo que os adapters
 * dependem apenas dos contratos (Port), nunca das implementações.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public ProcessarSaldoPort processarSaldoPort(SaldoRepositoryPort saldoRepositoryPort) {
        return new ProcessarSaldoUseCaseImpl(saldoRepositoryPort);
    }

    @Bean
    public BuscarSaldoPort buscarSaldoPort(SaldoRepositoryPort saldoRepositoryPort) {
        return new BuscarSaldoUseCaseImpl(saldoRepositoryPort);
    }
}
