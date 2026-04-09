package com.empresa.pedidos.domain;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.StatusPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * TESTE UNITARIO DE DOMINIO.
 *
 * Testa a logica de negocio da entidade Pedido.
 * Zero dependencias de Spring, banco ou HTTP — roda em millisegundos.
 *
 * Esta e a camada mais importante de testar: e onde vivem as regras.
 */
class PedidoTest {

    @Test
    void deveCriarPedidoComStatusPendente() {
        Pedido pedido = Pedido.criar("Notebook", BigDecimal.valueOf(5000), Endereco.vazio());

        assertThat(pedido.getId()).isNotNull();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(pedido.isPendente()).isTrue();
    }

    @Test
    void deveConfirmarPedidoPendente() {
        Pedido pedido = Pedido.criar("Notebook", BigDecimal.valueOf(5000), Endereco.vazio());

        pedido.confirmar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
    }

    @Test
    void deveLancarExcecaoAoConfirmarPedidoJaConfirmado() {
        Pedido pedido = Pedido.criar("Notebook", BigDecimal.valueOf(5000), Endereco.vazio());
        pedido.confirmar();

        assertThatThrownBy(pedido::confirmar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDENTE");
    }

    @Test
    void deveLancarExcecaoParaDescricaoVazia() {
        assertThatThrownBy(() -> Pedido.criar("", BigDecimal.valueOf(100), Endereco.vazio()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoParaValorZero() {
        assertThatThrownBy(() -> Pedido.criar("Produto", BigDecimal.ZERO, Endereco.vazio()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveCancelarPedido() {
        Pedido pedido = Pedido.criar("Notebook", BigDecimal.valueOf(5000), Endereco.vazio());

        pedido.cancelar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }
}
