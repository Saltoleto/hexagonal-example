package com.empresa.pedidos.application.usecase;

import com.empresa.pedidos.application.EnderecoServicePort;
import com.empresa.pedidos.application.PedidoRepositoryPort;
import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.StatusPedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TESTE DO USE CASE — CriarPedidoService.
 *
 * Valida a orquestração: ports são mockados, verifica que o use case
 * chama as dependências certas na ordem certa.
 * Regras de negócio são testadas em PedidoTest.
 */
@ExtendWith(MockitoExtension.class)
class CriarPedidoServiceTest {

    @Mock private PedidoRepositoryPort repositoryPort;
    @Mock private EnderecoServicePort enderecoServicePort;
    @InjectMocks private CriarPedidoService service;

    @Test
    void deveBuscarCepEPersistir() {
        var endereco = new Endereco("01310-100", "Av. Paulista", "São Paulo");
        when(enderecoServicePort.buscarPorCep("01310-100")).thenReturn(endereco);
        when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = service.executar("Monitor 4K", BigDecimal.valueOf(3000), "01310-100");

        assertThat(resultado.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(resultado.getEndereco().getCidade()).isEqualTo("São Paulo");
        verify(enderecoServicePort).buscarPorCep("01310-100");
        verify(repositoryPort).salvar(any());
    }

    @Test
    void naoDeveChamarApiExternaSemCep() {
        when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        service.executar("Teclado", BigDecimal.valueOf(500), null);

        verifyNoInteractions(enderecoServicePort);
        verify(repositoryPort).salvar(any());
    }

    @Test
    void deveRejeitarValorAcimaDoLimite() {
        assertThatThrownBy(() ->
                service.executar("Item", new BigDecimal("200000"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100000");

        verifyNoInteractions(repositoryPort);
    }
}
