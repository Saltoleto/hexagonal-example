package com.empresa.pedidos.application.usecase;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoDomainService;
import com.empresa.pedidos.domain.model.StatusPedido;
import com.empresa.pedidos.application.port.in.CriarPedidoUseCase;
import com.empresa.pedidos.application.port.out.EnderecoServicePort;
import com.empresa.pedidos.application.port.out.PedidoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TESTE DO USE CASE — CriarPedidoService.
 *
 * Valida a ORQUESTRAÇÃO: que o Use Case chama os ports certos
 * na ordem certa. Não testa regra de negócio (isso é papel do PedidoTest
 * e do PedidoDomainServiceTest).
 *
 * PedidoDomainService é @Spy (real, não mock) porque o Use Case
 * depende dele para validar — e queremos que a validação real rode.
 */
@ExtendWith(MockitoExtension.class)
class CriarPedidoServiceTest {

    @Spy
    private PedidoDomainService pedidoDomainService;

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

        // Verifica que o Use Case orquestrou corretamente: validou, buscou CEP, salvou
        verify(pedidoDomainService).validarCriacao("Monitor 4K", BigDecimal.valueOf(3000));
        verify(enderecoServicePort).buscarPorCep("01310-100");
        verify(repositoryPort).salvar(any());
    }

    @Test
    void deveCriarPedidoSemCepSemChamarApiExterna() {
        when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new CriarPedidoUseCase.Command("Teclado", BigDecimal.valueOf(500), null);
        service.executar(command);

        // Use Case não deve chamar API externa se CEP não foi informado
        verifyNoInteractions(enderecoServicePort);
        verify(repositoryPort).salvar(any());
    }

    @Test
    void deveRejeitarPedidoComValorAcimaDoLimite() {
        var command = new CriarPedidoUseCase.Command("Item caro", BigDecimal.valueOf(200000), null);

        // A regra (limite de valor) vive no PedidoDomainService — o Use Case apenas delega
        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100000");

        // Se a regra falhou, o Use Case não deve ter persistido nada
        verifyNoInteractions(repositoryPort);
    }
}
