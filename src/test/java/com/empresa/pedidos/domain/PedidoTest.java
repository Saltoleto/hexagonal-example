package com.empresa.pedidos.domain;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.StatusPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * TESTE DA ENTIDADE DE DOMÍNIO — Pedido.
 *
 * Testa as INVARIANTES DE ESTADO da entidade:
 * transições de status e suas restrições.
 *
 * Não testa validação de criação — isso é papel do PedidoDomainServiceTest.
 */
class PedidoTest {

    private Pedido pedidoPendente() {
        return Pedido.criar("Notebook", BigDecimal.valueOf(5000), Endereco.vazio());
    }

    @Test
    void deveCriarPedidoComStatusPendente() {
        Pedido pedido = pedidoPendente();

        assertThat(pedido.getId()).isNotNull();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(pedido.isPendente()).isTrue();
    }

    @Test
    void deveConfirmarPedidoPendente() {
        Pedido pedido = pedidoPendente();
        pedido.confirmar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
    }

    @Test
    void deveLancarExcecaoAoConfirmarPedidoJaConfirmado() {
        Pedido pedido = pedidoPendente();
        pedido.confirmar();

        // INVARIANTE: não pode confirmar duas vezes
        assertThatThrownBy(pedido::confirmar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDENTE");
    }

    @Test
    void deveLancarExcecaoAoConfirmarPedidoCancelado() {
        Pedido pedido = pedidoPendente();
        pedido.cancelar();

        // INVARIANTE: cancelado não pode ser confirmado
        assertThatThrownBy(pedido::confirmar)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deveCancelarPedido() {
        Pedido pedido = pedidoPendente();
        pedido.cancelar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }

    @Test
    void deveLancarExcecaoAoCancelarPedidoJaCancelado() {
        Pedido pedido = pedidoPendente();
        pedido.cancelar();

        // INVARIANTE: não pode cancelar duas vezes
        assertThatThrownBy(pedido::cancelar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelado");
    }
}
