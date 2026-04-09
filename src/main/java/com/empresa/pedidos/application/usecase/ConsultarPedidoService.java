package com.empresa.pedidos.application.usecase;

import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoNaoEncontradoException;
import com.empresa.pedidos.application.port.in.ConsultarPedidoUseCase;
import com.empresa.pedidos.application.port.out.PedidoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * USE CASE — Consultar Pedido.
 *
 * Responsabilidade: ORQUESTRAR consultas de pedidos.
 * Delega ao port out a busca; lança exceção de domínio se não encontrado.
 *
 * Não há regra de negócio aqui — a exceção PedidoNaoEncontradoException
 * é definida no domínio; o Use Case apenas decide quando lançá-la.
 *
 * @Transactional(readOnly = true) — otimização: evita flush do Hibernate
 * e pode usar réplicas de leitura se configurado.
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
        // Orquestra: delega ao port, trata ausência com exceção de domínio
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id.toString()));
    }

    @Override
    public List<Pedido> listarTodos() {
        return repositoryPort.buscarTodos();
    }
}
