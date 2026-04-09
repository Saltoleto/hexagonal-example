package com.empresa.pedidos.application;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.StatusPedido;
import com.empresa.pedidos.domain.port.in.CriarPedidoUseCase;
import com.empresa.pedidos.domain.port.out.EnderecoServicePort;
import com.empresa.pedidos.domain.port.out.PedidoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TESTE DO APPLICATION SERVICE.
 *
 * Testa a orquestracao — os ports sao mockados.
 * Confirma que o service chama os ports certos com os dados certos.
 *
 * Nao testa regra de negocio (isso e papel do PedidoTest).
 */
@ExtendWith(MockitoExtension.class)
class CriarPedidoServiceTest {

    @Mock
    private PedidoRepositoryPort repositoryPort;

    @Mock
    private EnderecoServicePort enderecoServicePort;

    @InjectMocks
    private CriarPedidoService service;

    @Test
    void deveCriarPedidoBuscandoEnderecoPorCep() {
        var endereco = new Endereco("01310-100", "Av. Paulista", "Sao Paulo");
        when(enderecoServicePort.buscarPorCep("01310-100")).thenReturn(endereco);
        when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new CriarPedidoUseCase.Command("Monitor 4K", BigDecimal.valueOf(3000), "01310-100");
        Pedido resultado = service.executar(command);

        assertThat(resultado.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(resultado.getEndereco().getCidade()).isEqualTo("Sao Paulo");

        verify(enderecoServicePort).buscarPorCep("01310-100");
        verify(repositoryPort).salvar(any());
    }

    @Test
    void deveCriarPedidoSemCepSemChamarApiExterna() {
        when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new CriarPedidoUseCase.Command("Teclado", BigDecimal.valueOf(500), null);
        service.executar(command);

        verifyNoInteractions(enderecoServicePort);
        verify(repositoryPort).salvar(any());
    }
}
