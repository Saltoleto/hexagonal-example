package com.empresa.pedidos.domain.model;

import java.util.Objects;

/**
 * Value Object — Endereço imutável.
 *
 * Value Objects não têm identidade própria; são iguais se todos os campos
 * forem iguais. Implementamos equals/hashCode baseado nos atributos.
 *
 * REGRA HEXAGONAL: sem Spring, sem JPA.
 */
public final class Endereco {

    private final String cep;
    private final String logradouro;
    private final String cidade;

    public Endereco(String cep, String logradouro, String cidade) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.cidade = cidade;
    }

    public static Endereco vazio() {
        return new Endereco(null, null, null);
    }

    public String getCep()        { return cep; }
    public String getLogradouro() { return logradouro; }
    public String getCidade()     { return cidade; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Endereco e)) return false;
        return Objects.equals(cep, e.cep)
                && Objects.equals(logradouro, e.logradouro)
                && Objects.equals(cidade, e.cidade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cep, logradouro, cidade);
    }

    @Override
    public String toString() {
        return "Endereco{cep='%s', logradouro='%s', cidade='%s'}".formatted(cep, logradouro, cidade);
    }
}
