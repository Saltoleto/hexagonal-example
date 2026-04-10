package com.empresa.pedidos.domain;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.StatusPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * TESTE UNITÁRIO DE DOMÍNIO.
 *
 * Testa invariantes de estado e regras de criação da entidade Pedido.
 * Zero dependências de Spring, JPA ou qualquer infraestrutura.
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
    void deveRejeitarDescricaoCurta() {
        assertThatThrownBy(() -> Pedido.criar("AB", BigDecimal.valueOf(100), Endereco.vazio()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 caracteres");
    }

    @Test
    void deveRejeitarValorZero() {
        assertThatThrownBy(() -> Pedido.criar("Produto", BigDecimal.ZERO, Endereco.vazio()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarValorAcimaDoLimite() {
        assertThatThrownBy(() -> Pedido.criar("Produto", new BigDecimal("100001"), Endereco.vazio()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100000");
    }

    @Test
    void deveConfirmarPedidoPendente() {
        Pedido pedido = pedidoPendente();
        pedido.confirmar();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
    }

    @Test
    void deveLancarExcecaoAoConfirmarDuasVezes() {
        Pedido pedido = pedidoPendente();
        pedido.confirmar();
        assertThatThrownBy(pedido::confirmar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDENTE");
    }

    @Test
    void deveCancelarPedido() {
        Pedido pedido = pedidoPendente();
        pedido.cancelar();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }

    @Test
    void deveLancarExcecaoAoCancelarDuasVezes() {
        Pedido pedido = pedidoPendente();
        pedido.cancelar();
        assertThatThrownBy(pedido::cancelar)
                .isInstanceOf(IllegalStateException.class);
    }
}
