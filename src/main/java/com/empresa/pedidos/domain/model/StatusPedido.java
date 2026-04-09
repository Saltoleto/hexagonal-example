package com.empresa.pedidos.domain.model;

/**
 * Enum de domínio — estados possíveis de um Pedido.
 * Contém a descrição legível para exibição.
 */
public enum StatusPedido {

    PENDENTE("Aguardando confirmação"),
    CONFIRMADO("Confirmado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
