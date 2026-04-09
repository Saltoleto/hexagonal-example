package com.empresa.pedidos.domain.model;

import java.math.BigDecimal;

/**
 * DOMAIN SERVICE — lógica de negócio que não pertence a uma única entidade.
 *
 * Usado quando uma regra envolve conceitos do domínio que vão além do estado
 * interno de Pedido. Ex.: validações cruzadas, políticas de criação, cálculos
 * que dependem de múltiplos objetos de domínio.
 *
 * Diferença do Use Case:
 *   - Domain Service  → É REGRA DE NEGÓCIO. Diz "o que pode ou não pode".
 *   - Use Case        → É FLUXO. Diz "busque isso, chame aquilo, persista".
 *
 * REGRA HEXAGONAL: sem Spring, sem JPA, sem HTTP — Java puro.
 */
public class PedidoDomainService {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("100000.00");

    /**
     * Valida as regras de negócio para criação de um pedido.
     * Lança exceção de domínio se alguma regra for violada.
     *
     * Esta lógica está aqui — e não no Use Case — porque é uma REGRA:
     * "Um pedido não pode ser criado com valor acima de R$ 100.000"
     * é conhecimento do domínio, não detalhe de fluxo.
     */
    public void validarCriacao(String descricao, BigDecimal valor) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        if (descricao.length() < 3) {
            throw new IllegalArgumentException("Descrição deve ter ao menos 3 caracteres");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            throw new IllegalArgumentException(
                "Valor não pode ultrapassar R$ %s".formatted(VALOR_MAXIMO));
        }
    }
}
