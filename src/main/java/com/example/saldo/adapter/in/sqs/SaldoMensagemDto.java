package com.example.saldo.adapter.in.sqs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que representa o payload JSON recebido da fila SQS.
 * Totalmente desacoplado do domínio.
 *
 * Exemplo de mensagem esperada:
 * {
 *   "conta_id": "CC-12345",
 *   "valor": 1500.00,
 *   "moeda": "BRL",
 *   "tipo": "CREDITO",
 *   "data_referencia": "2024-04-01T10:00:00"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaldoMensagemDto {

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

    // Getters e Setters
    public String getContaId() { return contaId; }
    public void setContaId(String contaId) { this.contaId = contaId; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getMoeda() { return moeda; }
    public void setMoeda(String moeda) { this.moeda = moeda; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDateTime getDataReferencia() { return dataReferencia; }
    public void setDataReferencia(LocalDateTime dataReferencia) { this.dataReferencia = dataReferencia; }
}
