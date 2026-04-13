package com.example.saldo.application.usecase;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.model.TipoSaldo;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessarSaldoUseCaseTest {

    @Mock
    private SaldoRepositoryPort saldoRepositoryPort;

    @InjectMocks
    private ProcessarSaldoUseCaseImpl useCase;

    private Saldo saldoValido;

    @BeforeEach
    void setUp() {
        saldoValido = new Saldo(
            "CC-001",
            new BigDecimal("1500.00"),
            "BRL",
            TipoSaldo.CREDITO,
            LocalDateTime.now()
        );
    }

    @Test
    void deveProcessarSaldoValido() {
        Saldo saldoSalvo = new Saldo("CC-001", new BigDecimal("1500.00"), "BRL", TipoSaldo.CREDITO, LocalDateTime.now());
        saldoSalvo.setId(1L);
        when(saldoRepositoryPort.salvar(any())).thenReturn(saldoSalvo);

        Saldo resultado = useCase.processar(saldoValido);

        assertThat(resultado.getId()).isEqualTo(1L);
        verify(saldoRepositoryPort, times(1)).salvar(saldoValido);
    }

    @Test
    void deveRejeitarSaldoSemContaId() {
        Saldo invalido = new Saldo(null, new BigDecimal("100"), "BRL", TipoSaldo.DEBITO, LocalDateTime.now());

        assertThatThrownBy(() -> useCase.processar(invalido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Saldo inválido");

        verify(saldoRepositoryPort, never()).salvar(any());
    }

    @Test
    void deveRejeitarSaldoSemValor() {
        Saldo invalido = new Saldo("CC-001", null, "BRL", TipoSaldo.CREDITO, LocalDateTime.now());

        assertThatThrownBy(() -> useCase.processar(invalido))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
