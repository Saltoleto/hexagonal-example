package com.empresa.pedidos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ENTIDADE DE DOMÍNIO.
 *
 * Responsabilidades:
 *   - Estado do pedido
 *   - Invariantes de transição de estado (confirmar, cancelar)
 *   - Regras de criação (validação absorvida — ver DA-03 no README)
 *
 * Sem Spring, sem JPA, sem framework externo.
 */
public class Pedido {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("100000.00");

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
     * Cria um novo pedido validando as regras de negócio.
     *
     * Simplificação adotada (DA-03): validação absorvida pela entidade.
     * Em domínios mais ricos, considerar PedidoDomainService.
     */
    public static Pedido criar(String descricao, BigDecimal valor, Endereco endereco) {
        if (descricao == null || descricao.isBlank() || descricao.length() < 3) {
            throw new IllegalArgumentException("Descrição deve ter ao menos 3 caracteres");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            throw new IllegalArgumentException(
                "Valor não pode ultrapassar R$ %s".formatted(VALOR_MAXIMO));
        }
        return new Pedido(UUID.randomUUID(), descricao, valor,
                StatusPedido.PENDENTE, endereco,
                LocalDateTime.now(), LocalDateTime.now());
    }

    /** Reconstrói um pedido existente vindo da persistência. */
    public static Pedido reconstituir(UUID id, String descricao, BigDecimal valor,
                                      StatusPedido status, Endereco endereco,
                                      LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        return new Pedido(id, descricao, valor, status, endereco, criadoEm, atualizadoEm);
    }

    // ── Comportamentos — invariantes de estado ──────────────────────────────

    /** INVARIANTE: só pedidos PENDENTE podem ser confirmados. */
    public void confirmar() {
        if (this.status != StatusPedido.PENDENTE) {
            throw new IllegalStateException("Apenas pedidos PENDENTE podem ser confirmados");
        }
        this.status = StatusPedido.CONFIRMADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    /** INVARIANTE: pedido já cancelado não pode ser cancelado novamente. */
    public void cancelar() {
        if (this.status == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Pedido já está cancelado");
        }
        this.status = StatusPedido.CANCELADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public boolean isPendente()              { return this.status == StatusPedido.PENDENTE; }
    public UUID getId()                      { return id; }
    public String getDescricao()             { return descricao; }
    public BigDecimal getValor()             { return valor; }
    public StatusPedido getStatus()          { return status; }
    public Endereco getEndereco()            { return endereco; }
    public LocalDateTime getCriadoEm()      { return criadoEm; }
    public LocalDateTime getAtualizadoEm()  { return atualizadoEm; }
}
