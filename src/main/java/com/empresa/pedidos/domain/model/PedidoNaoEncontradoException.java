package com.empresa.pedidos.domain.model;

/**
 * Exceção de domínio — lançada quando um Pedido não é encontrado.
 * Exceções de domínio vivem no domain; não são exceções de infraestrutura.
 */
public class PedidoNaoEncontradoException extends RuntimeException {

    private final String pedidoId;

    public PedidoNaoEncontradoException(String pedidoId) {
        super("Pedido não encontrado: " + pedidoId);
        this.pedidoId = pedidoId;
    }

    public String getPedidoId() {
        return pedidoId;
    }
}
