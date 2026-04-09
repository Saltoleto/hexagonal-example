package com.empresa.pedidos.domain.port.in;

import com.empresa.pedidos.domain.model.Pedido;

import java.util.UUID;

/**
 * PORT DE ENTRADA (Driving Port)
 *
 * Contrato para atualização de status de um pedido.
 * Confirmar e cancelar são operações de negócio distintas.
 */
public interface AtualizarStatusPedidoUseCase {

    Pedido confirmar(UUID id);

    Pedido cancelar(UUID id);
}
