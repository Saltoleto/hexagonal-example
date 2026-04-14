package com.example.saldo.application.usecase;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.model.TipoSaldo;
import com.example.saldo.core.port.in.ProcessarSaldoPort;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import com.example.saldo.core.usecase.ProcessarSaldoUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessarSaldoPortTest {

    @Mock
    private SaldoRepositoryPort saldoRepositoryPort;

    private ProcessarSaldoPort port;

    @BeforeEach
    void setUp() {
        port = new ProcessarSaldoUseCaseImpl(saldoRepositoryPort);
    }

    @Test
    void deveProcessarSaldoValido() {
        Saldo entrada = new Saldo("CC-001", new BigDecimal("1500.00"), "BRL", TipoSaldo.CREDITO, LocalDateTime.now());
        Saldo salvo = new Saldo("CC-001", new BigDecimal("1500.00"), "BRL", TipoSaldo.CREDITO, LocalDateTime.now());
        salvo.setId(1L);

        when(saldoRepositoryPort.salvar(any())).thenReturn(salvo);

        Saldo resultado = port.processar(entrada);

        assertThat(resultado.getId()).isEqualTo(1L);
        verify(saldoRepositoryPort, times(1)).salvar(entrada);
    }

    @Test
    void deveRejeitarSaldoSemContaId() {
        Saldo invalido = new Saldo(null, new BigDecimal("100"), "BRL", TipoSaldo.DEBITO, LocalDateTime.now());

        assertThatThrownBy(() -> port.processar(invalido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Saldo inválido");

        verify(saldoRepositoryPort, never()).salvar(any());
    }

    @Test
    void deveRejeitarSaldoSemValor() {
        Saldo invalido = new Saldo("CC-001", null, "BRL", TipoSaldo.CREDITO, LocalDateTime.now());

        assertThatThrownBy(() -> port.processar(invalido))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
