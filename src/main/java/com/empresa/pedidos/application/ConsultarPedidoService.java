package com.empresa.pedidos.application;

import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoNaoEncontradoException;
import com.empresa.pedidos.domain.port.in.ConsultarPedidoUseCase;
import com.empresa.pedidos.domain.port.out.PedidoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * APPLICATION SERVICE — Implementa ConsultarPedidoUseCase.
 *
 * Operações de leitura — read-only transactions para melhor performance.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarPedidoService implements ConsultarPedidoUseCase {

    private final PedidoRepositoryPort repositoryPort;

    public ConsultarPedidoService(PedidoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Pedido buscarPorId(UUID id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id.toString()));
    }

    @Override
    public List<Pedido> listarTodos() {
        return repositoryPort.buscarTodos();
    }
}
