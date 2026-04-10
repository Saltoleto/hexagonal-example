package com.empresa.pedidos.adapter.out.restclient;

import com.empresa.pedidos.application.EnderecoServicePort;
import com.empresa.pedidos.domain.model.Endereco;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * ADAPTER DE SAÍDA — REST Client para ViaCEP.
 *
 * Implementa EnderecoServicePort (contrato da camada de aplicação).
 * Fallback para Endereco.vazio() em caso de falha — decisão DA-07.
 */
@Component
public class EnderecoRestAdapter implements EnderecoServicePort {

    private static final Logger log = LoggerFactory.getLogger(EnderecoRestAdapter.class);

    private final RestTemplate restTemplate;
    private final String viaCepBaseUrl;

    public EnderecoRestAdapter(RestTemplate restTemplate,
                                @Value("${cliente.viacep.base-url}") String viaCepBaseUrl) {
        this.restTemplate = restTemplate;
        this.viaCepBaseUrl = viaCepBaseUrl;
    }

    @Override
    public Endereco buscarPorCep(String cep) {
        String cepLimpo = cep.replaceAll("[^0-9]", "");
        String url = "%s/%s/json".formatted(viaCepBaseUrl, cepLimpo);
        try {
            ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);
            if (response == null || response.isCepInvalido()) {
                log.warn("CEP não encontrado: {}", cep);
                return Endereco.vazio();
            }
            return new Endereco(response.cep(), response.logradouro(), response.localidade());
        } catch (RestClientException ex) {
            log.error("Erro ao consultar ViaCEP para CEP {}: {}", cep, ex.getMessage());
            return Endereco.vazio();
        }
    }
}
