package com.example.saldo.infrastructure.adapter.in.sqs;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.port.in.ProcessarSaldoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaldoSqsListenerTest {

    @Mock
    private ProcessarSaldoPort processarSaldoPort;

    @InjectMocks
    private SaldoSqsListener listener;

    @Test
    void deveConverterMensagemEProcessar() {
        SaldoMensagemDto dto = new SaldoMensagemDto();
        dto.setContaId("CC-999");
        dto.setValor(new BigDecimal("500.00"));
        dto.setMoeda("BRL");
        dto.setTipo("CREDITO");
        dto.setDataReferencia(LocalDateTime.now());

        when(processarSaldoUseCase.processar(any())).thenReturn(new Saldo());

        listener.onMessage(dto);

        ArgumentCaptor<Saldo> captor = ArgumentCaptor.forClass(Saldo.class);
        verify(processarSaldoUseCase).processar(captor.capture());

        Saldo capturado = captor.getValue();
        assertThat(capturado.getContaId()).isEqualTo("CC-999");
        assertThat(capturado.getValor()).isEqualByComparingTo("500.00");
    }

    @Test
    void deveTratarTipoInvalido() {
        SaldoMensagemDto dto = new SaldoMensagemDto();
        dto.setContaId("CC-001");
        dto.setValor(BigDecimal.TEN);
        dto.setMoeda("BRL");
        dto.setTipo("TIPO_INEXISTENTE");

        // Não deve lançar exceção — deve logar e descartar
        assertThatCode(() -> listener.onMessage(dto))
            .doesNotThrowAnyException();

        verify(processarSaldoUseCase, never()).processar(any());
    }
}
