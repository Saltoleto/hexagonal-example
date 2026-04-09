package com.empresa.pedidos.application.port.out;

import com.empresa.pedidos.domain.model.Pedido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PORT DE SAÍDA (Driven Port) — Persistência
 *
 * O domínio define aqui o que precisa do banco de dados.
 * A implementação concreta (JPA, JDBC, etc.) vive no adapter.out.persistence.
 *
 * REGRA: o domínio nunca sabe que existe JPA, Hibernate ou PostgreSQL.
 */
public interface PedidoRepositoryPort {

    Pedido salvar(Pedido pedido);

    Optional<Pedido> buscarPorId(UUID id);

    List<Pedido> buscarTodos();
}
