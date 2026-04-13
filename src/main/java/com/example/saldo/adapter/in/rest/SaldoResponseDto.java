package com.example.saldo.adapter.in.rest;

import com.example.saldo.core.model.Saldo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de saída do endpoint REST.
 * Isolado do domínio — o cliente da API não conhece a entidade Saldo.
 */
public class SaldoResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("conta_id")
    private String contaId;

    @JsonProperty("valor")
    private BigDecimal valor;

    @JsonProperty("moeda")
    private String moeda;

    @JsonProperty("tipo")
    private String tipo;

    @JsonProperty("data_referencia")
    private LocalDateTime dataReferencia;

    @JsonProperty("data_processamento")
    private LocalDateTime dataProcessamento;

    public static SaldoResponseDto from(Saldo saldo) {
        SaldoResponseDto dto = new SaldoResponseDto();
        dto.id = saldo.getId();
        dto.contaId = saldo.getContaId();
        dto.valor = saldo.getValor();
        dto.moeda = saldo.getMoeda();
        dto.tipo = saldo.getTipo().name();
        dto.dataReferencia = saldo.getDataReferencia();
        dto.dataProcessamento = saldo.getDataProcessamento();
        return dto;
    }

    // Getters
    public Long getId() { return id; }
    public String getContaId() { return contaId; }
    public BigDecimal getValor() { return valor; }
    public String getMoeda() { return moeda; }
    public String getTipo() { return tipo; }
    public LocalDateTime getDataReferencia() { return dataReferencia; }
    public LocalDateTime getDataProcessamento() { return dataProcessamento; }
}
