package com.empresa.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point da aplicacao Spring Boot.
 *
 * Unica responsabilidade: iniciar o contexto Spring.
 * Nenhuma logica de negocio aqui.
 */
@SpringBootApplication
public class PedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedidosApplication.class, args);
    }
}
