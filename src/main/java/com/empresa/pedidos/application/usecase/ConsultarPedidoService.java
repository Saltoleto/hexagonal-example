package com.empresa.pedidos.application.usecase;

import com.empresa.pedidos.application.PedidoRepositoryPort;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * USE CASE — Consultar Pedido.
 *
 * Operações de leitura com @Transactional(readOnly = true):
 * evita flush do Hibernate e habilita uso de réplicas de leitura (DA-08).
 */
@Service
@Transactional(readOnly = true)
public class ConsultarPedidoService {

    private final PedidoRepositoryPort repositoryPort;

    public ConsultarPedidoService(PedidoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    public Pedido buscarPorId(UUID id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id.toString()));
    }

    public List<Pedido> listarTodos() {
        return repositoryPort.buscarTodos();
    }
}
