package com.example.saldo.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade de domínio pura — sem anotações de framework.
 * Representa um saldo recebido via mensagem.
 */
public class Saldo {

    private Long id;
    private String contaId;
    private BigDecimal valor;
    private String moeda;
    private TipoSaldo tipo;
    private LocalDateTime dataReferencia;
    private LocalDateTime dataProcessamento;

    public Saldo() {}

    public Saldo(String contaId, BigDecimal valor, String moeda,
                 TipoSaldo tipo, LocalDateTime dataReferencia) {
        this.contaId = contaId;
        this.valor = valor;
        this.moeda = moeda;
        this.tipo = tipo;
        this.dataReferencia = dataReferencia;
        this.dataProcessamento = LocalDateTime.now();
    }

    // Regra de domínio: saldo não pode ser nulo
    public boolean isValido() {
        return contaId != null && !contaId.isBlank()
                && valor != null
                && moeda != null && !moeda.isBlank()
                && tipo != null;
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

    public TipoSaldo getTipo() { return tipo; }
    public void setTipo(TipoSaldo tipo) { this.tipo = tipo; }

    public LocalDateTime getDataReferencia() { return dataReferencia; }
    public void setDataReferencia(LocalDateTime dataReferencia) { this.dataReferencia = dataReferencia; }

    public LocalDateTime getDataProcessamento() { return dataProcessamento; }
    public void setDataProcessamento(LocalDateTime dataProcessamento) { this.dataProcessamento = dataProcessamento; }

    @Override
    public String toString() {
        return "Saldo{contaId='" + contaId + "', valor=" + valor + ", moeda='" + moeda + "', tipo=" + tipo + "}";
    }
}
