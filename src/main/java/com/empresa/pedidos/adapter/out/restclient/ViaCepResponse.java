package com.empresa.pedidos.adapter.out.restclient;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO DA API EXTERNA — ViaCEP.
 *
 * Mapeia exatamente o JSON retornado pela API externa.
 * Fica isolado no adapter para que mudancas na API externa
 * nao vazem para o dominio.
 *
 * Se a API mudar o nome de um campo, so este record muda.
 */
public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        String ibge,
        String gia,
        String ddd,
        String siafi,

        @JsonProperty("erro")
        Boolean erro
) {
    public boolean isCepInvalido() {
        return Boolean.TRUE.equals(erro);
    }
}
