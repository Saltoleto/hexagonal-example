package com.empresa.pedidos.domain;

import com.empresa.pedidos.domain.model.PedidoDomainService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * TESTE DO DOMAIN SERVICE.
 *
 * Testa exclusivamente as REGRAS DE NEGÓCIO de criação.
 * Zero dependências de Spring, JPA ou qualquer infraestrutura.
 */
class PedidoDomainServiceTest {

    private final PedidoDomainService service = new PedidoDomainService();

    @Test
    void devePassarValidacaoComDadosValidos() {
        assertThatNoException()
                .isThrownBy(() -> service.validarCriacao("Notebook", BigDecimal.valueOf(5000)));
    }

    @Test
    void deveRejeitarDescricaoVazia() {
        assertThatThrownBy(() -> service.validarCriacao("", BigDecimal.valueOf(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obrigatória");
    }

    @Test
    void deveRejeitarDescricaoCurta() {
        assertThatThrownBy(() -> service.validarCriacao("AB", BigDecimal.valueOf(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 caracteres");
    }

    @Test
    void deveRejeitarValorZero() {
        assertThatThrownBy(() -> service.validarCriacao("Produto", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positivo");
    }

    @Test
    void deveRejeitarValorAcimaDoLimite() {
        assertThatThrownBy(() -> service.validarCriacao("Produto", new BigDecimal("100001.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100000");
    }
}
