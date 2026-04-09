package com.empresa.pedidos.adapter.out.persistence;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE DO ADAPTER DE PERSISTENCIA — @DataJpaTest.
 *
 * Usa H2 em memoria (profile test). Testa o mapeamento
 * Entity <-> Domain e as operacoes de banco reais.
 *
 * Nao carrega o contexto web nem os outros adapters.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({PedidoJpaAdapter.class, PedidoEntityMapper.class})
class PedidoJpaAdapterTest {

    @Autowired
    private PedidoJpaAdapter adapter;

    @Test
    void deveSalvarERecuperarPedido() {
        var pedido = Pedido.criar("SSD 1TB", BigDecimal.valueOf(450),
                new Endereco("01310-100", "Av. Paulista", "Sao Paulo"));

        Pedido salvo = adapter.salvar(pedido);
        Optional<Pedido> encontrado = adapter.buscarPorId(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getDescricao()).isEqualTo("SSD 1TB");
        assertThat(encontrado.get().getEndereco().getCidade()).isEqualTo("Sao Paulo");
    }

    @Test
    void deveRetornarEmptyParaIdInexistente() {
        Optional<Pedido> resultado = adapter.buscarPorId(java.util.UUID.randomUUID());
        assertThat(resultado).isEmpty();
    }
}
