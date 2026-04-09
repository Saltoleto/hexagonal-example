package com.empresa.pedidos.adapter.out.restclient;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.port.out.EnderecoServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * ADAPTER DE SAIDA — REST Client para API externa (ViaCEP).
 *
 * Implementa EnderecoServicePort (contrato do dominio).
 * Isola toda a complexidade de comunicacao HTTP neste adapter:
 *  - URL da API
 *  - Tratamento de erros HTTP
 *  - Fallback em caso de falha (retorna Endereco.vazio())
 *  - Mapeamento ViaCepResponse -> Endereco (dominio)
 *
 * O dominio enxerga apenas: "me da o endereco deste CEP".
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
                log.warn("CEP nao encontrado ou invalido: {}", cep);
                return Endereco.vazio();
            }

            return new Endereco(
                    response.cep(),
                    response.logradouro(),
                    response.localidade()
            );
        } catch (RestClientException ex) {
            log.error("Erro ao consultar ViaCEP para o CEP {}: {}", cep, ex.getMessage());
            // Fallback: nao falha a criacao do pedido por indisponibilidade de API externa
            return Endereco.vazio();
        }
    }
}
