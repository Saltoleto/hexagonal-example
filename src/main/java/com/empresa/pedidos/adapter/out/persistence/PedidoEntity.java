package com.empresa.pedidos.adapter.out.persistence;

import com.empresa.pedidos.domain.model.StatusPedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ENTIDADE JPA — detalhe de infraestrutura.
 *
 * Esta classe existe exclusivamente para o mapeamento ORM.
 * E completamente separada da entidade de dominio (Pedido.java).
 *
 * Por que separar?
 *  - A entidade de dominio pode ter comportamentos e invariantes
 *    que o JPA nao sabe gerenciar (construtor privado, factory methods)
 *  - O schema do banco pode evoluir independente do modelo de dominio
 *  - Evita anotacoes de infraestrutura no nucleo do sistema
 */
@Entity
@Table(name = "pedido")
public class PedidoEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusPedido status;

    @Column(length = 9)
    private String cep;

    @Column(length = 255)
    private String logradouro;

    @Column(length = 100)
    private String cidade;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    // JPA exige construtor padrao
    protected PedidoEntity() {}

    public PedidoEntity(UUID id, String descricao, BigDecimal valor, StatusPedido status,
                        String cep, String logradouro, String cidade,
                        LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.status = status;
        this.cep = cep;
        this.logradouro = logradouro;
        this.cidade = cidade;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId()                     { return id; }
    public String getDescricao()            { return descricao; }
    public BigDecimal getValor()            { return valor; }
    public StatusPedido getStatus()         { return status; }
    public String getCep()                  { return cep; }
    public String getLogradouro()           { return logradouro; }
    public String getCidade()               { return cidade; }
    public LocalDateTime getCriadoEm()     { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
