package com.example.saldo.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade JPA — detalhe de infraestrutura.
 * Completamente isolada do domínio através do mapper.
 */
@Entity
@Table(name = "saldos", indexes = {
    @Index(name = "idx_saldos_conta_id", columnList = "conta_id"),
    @Index(name = "idx_saldos_data_referencia", columnList = "data_referencia")
})
public class SaldoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_id", nullable = false, length = 50)
    private String contaId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 3)
    private String moeda;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoSaldoJpa tipo;

    @Column(name = "data_referencia")
    private LocalDateTime dataReferencia;

    @Column(name = "data_processamento", nullable = false)
    private LocalDateTime dataProcessamento;

    // Enum interno para mapeamento JPA
    public enum TipoSaldoJpa {
        CREDITO, DEBITO, DISPONIVEL, BLOQUEADO
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContaId() { return contaId; }
    public void setContaId(String contaId) { this.contaId = contaId; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getMoeda() { return moeda; }
    public void setMoeda(String moeda) { this.moeda = moeda; }

    public TipoSaldoJpa getTipo() { return tipo; }
    public void setTipo(TipoSaldoJpa tipo) { this.tipo = tipo; }

    public LocalDateTime getDataReferencia() { return dataReferencia; }
    public void setDataReferencia(LocalDateTime dataReferencia) { this.dataReferencia = dataReferencia; }

    public LocalDateTime getDataProcessamento() { return dataProcessamento; }
    public void setDataProcessamento(LocalDateTime dataProcessamento) { this.dataProcessamento = dataProcessamento; }
}
