package com.empresa.pedidos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ENTIDADE DE DOMÍNIO — representa um Pedido.
 *
 * Responsabilidades desta classe:
 *   - Guardar o estado do pedido
 *   - Executar transições de estado com suas invariantes
 *     ("um pedido CONFIRMADO não pode ser confirmado novamente")
 *
 * O que NÃO fica aqui:
 *   - Validação de criação (vai para PedidoDomainService)
 *   - Fluxo de persistência ou chamadas externas (vai para o Use Case)
 *
 * REGRA HEXAGONAL: sem Spring, sem JPA, sem framework externo.
 */
public class Pedido {

    private final UUID id;
    private final String descricao;
    private final BigDecimal valor;
    private StatusPedido status;
    private final Endereco endereco;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

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
     * Cria um novo pedido.
     * Pré-condição: as regras de negócio já foram validadas pelo PedidoDomainService.
     */
    public static Pedido criar(String descricao, BigDecimal valor, Endereco endereco) {
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

    // ── Comportamentos de domínio (invariantes de estado) ──────────────────

    /**
     * Confirma o pedido.
     * INVARIANTE: só pedidos PENDENTE podem ser confirmados.
     * Esta regra vive na entidade porque é sobre o estado interno dela.
     */
    public void confirmar() {
        if (this.status != StatusPedido.PENDENTE) {
            throw new IllegalStateException("Apenas pedidos PENDENTE podem ser confirmados");
        }
        this.status = StatusPedido.CONFIRMADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Cancela o pedido.
     * INVARIANTE: um pedido já cancelado não pode ser cancelado novamente.
     */
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

    // ── Getters (sem setters — estado muda apenas por comportamentos acima) ─

    public UUID getId()                     { return id; }
    public String getDescricao()            { return descricao; }
    public BigDecimal getValor()            { return valor; }
    public StatusPedido getStatus()         { return status; }
    public Endereco getEndereco()           { return endereco; }
    public LocalDateTime getCriadoEm()     { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
