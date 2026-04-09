package com.empresa.pedidos.domain.port.in;

import com.empresa.pedidos.domain.model.Pedido;

import java.util.List;
import java.util.UUID;

/**
 * PORT DE ENTRADA (Driving Port)
 *
 * Contrato para consultas de pedidos.
 * Separado do CriarPedidoUseCase por responsabilidade única:
 * comandos e queries são conceitos distintos (CQRS light).
 */
public interface ConsultarPedidoUseCase {

    Pedido buscarPorId(UUID id);

    List<Pedido> listarTodos();
}
