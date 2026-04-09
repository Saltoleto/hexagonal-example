package com.empresa.pedidos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de domínio — representa um Pedido no sistema.
 *
 * REGRA HEXAGONAL: esta classe não pode importar nada de Spring, JPA ou
 * qualquer framework externo. Ela contém apenas lógica de negócio pura.
 */
public class Pedido {

    private final UUID id;
    private String descricao;
    private BigDecimal valor;
    private StatusPedido status;
    private Endereco endereco;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Construtor privado — use os factory methods abaixo
    private Pedido(UUID id, String descricao, BigDecimal valor,
                   StatusPedido status, Endereco endereco,
                   LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.status = status;
        this.endereco = endereco;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    /**
     * Cria um novo pedido (ainda não persistido).
     */
    public static Pedido criar(String descricao, BigDecimal valor, Endereco endereco) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
        return new Pedido(
                UUID.randomUUID(),
                descricao,
                valor,
                StatusPedido.PENDENTE,
                endereco,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * Reconstrói um pedido já existente (vindo da persistência).
     */
    public static Pedido reconstituir(UUID id, String descricao, BigDecimal valor,
                                      StatusPedido status, Endereco endereco,
                                      LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        return new Pedido(id, descricao, valor, status, endereco, criadoEm, atualizadoEm);
    }

    // --- Comportamentos de domínio ---

    public void confirmar() {
        if (this.status != StatusPedido.PENDENTE) {
            throw new IllegalStateException("Apenas pedidos PENDENTE podem ser confirmados");
        }
        this.status = StatusPedido.CONFIRMADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void cancelar() {
        if (this.status == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Pedido já está cancelado");
        }
        this.status = StatusPedido.CANCELADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public boolean isPendente() {
        return this.status == StatusPedido.PENDENTE;
    }

    // --- Getters (sem setters — imutabilidade intencional) ---

    public UUID getId()                      { return id; }
    public String getDescricao()             { return descricao; }
    public BigDecimal getValor()             { return valor; }
    public StatusPedido getStatus()          { return status; }
    public Endereco getEndereco()            { return endereco; }
    public LocalDateTime getCriadoEm()      { return criadoEm; }
    public LocalDateTime getAtualizadoEm()  { return atualizadoEm; }
}
