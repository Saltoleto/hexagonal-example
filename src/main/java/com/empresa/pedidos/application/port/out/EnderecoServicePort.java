package com.empresa.pedidos.application.port.out;

import com.empresa.pedidos.domain.model.Endereco;

/**
 * PORT DE SAÍDA (Driven Port) — API Externa
 *
 * Contrato para busca de endereço por CEP.
 * A implementação concreta chama a API ViaCEP via HTTP —
 * o domínio não sabe disso, enxerga apenas esta interface.
 */
public interface EnderecoServicePort {

    /**
     * Busca o endereço correspondente ao CEP informado.
     * Retorna Endereco.vazio() se o CEP não for encontrado.
     */
    Endereco buscarPorCep(String cep);
}
