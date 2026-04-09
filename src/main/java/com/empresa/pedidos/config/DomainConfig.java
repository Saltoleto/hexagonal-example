package com.empresa.pedidos.config;

import com.empresa.pedidos.domain.model.PedidoDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CONFIGURAÇÃO DE DOMÍNIO.
 *
 * Expõe os Domain Services como beans Spring para que possam ser injetados
 * nos Use Cases (Application Services).
 *
 * Por que aqui e não @Service no próprio PedidoDomainService?
 * Porque o domínio não deve ter anotações de framework.
 * A configuração Spring fica na borda — não no núcleo.
 */
@Configuration
public class DomainConfig {

    @Bean
    public PedidoDomainService pedidoDomainService() {
        return new PedidoDomainService();
    }
}
