package com.empresa.pedidos.application;

import com.empresa.pedidos.domain.model.Endereco;

/**
 * PORT DE SAÍDA — API externa de endereço.
 *
 * O use case depende desta interface; o adapter REST implementa.
 * Retorna Endereco.vazio() se o CEP não for encontrado (DA-07 no README).
 */
public interface EnderecoServicePort {
    Endereco buscarPorCep(String cep);
}
