package com.empresa.pedidos.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * SPRING DATA JPA REPOSITORY.
 *
 * Interface gerenciada pelo Spring — nao e implementada manualmente.
 * Trabalha exclusivamente com PedidoEntity (nao com Pedido do dominio).
 *
 * Queries customizadas entram aqui como @Query ou metodos derivados.
 */
public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, UUID> {
    // Exemplo de query derivada (nao usada no demo, mas ilustrativa):
    // List<PedidoEntity> findByStatus(StatusPedido status);
}
