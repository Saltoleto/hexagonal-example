package com.empresa.pedidos.application;

import com.empresa.pedidos.domain.model.Pedido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PORT DE SAÍDA — Persistência.
 *
 * Contrato definido pela camada de aplicação: o use case dita o que precisa,
 * o adapter JPA implementa. O domínio não conhece esta interface.
 *
 * Localização em application/ (DA-01 no README): ports out existem para
 * servir os use cases, portanto vivem junto com eles.
 */
public interface PedidoRepositoryPort {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(UUID id);
    List<Pedido> buscarTodos();
}
