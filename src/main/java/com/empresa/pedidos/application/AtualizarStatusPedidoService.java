package com.empresa.pedidos.application;

import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoNaoEncontradoException;
import com.empresa.pedidos.domain.port.in.AtualizarStatusPedidoUseCase;
import com.empresa.pedidos.domain.port.out.PedidoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * APPLICATION SERVICE — Implementa AtualizarStatusPedidoUseCase.
 *
 * A lógica de transição de estado (confirmar/cancelar) vive na entidade Pedido.
 * Este service apenas orquestra: busca → muta → persiste.
 */
@Service
@Transactional
public class AtualizarStatusPedidoService implements AtualizarStatusPedidoUseCase {

    private final PedidoRepositoryPort repositoryPort;

    public AtualizarStatusPedidoService(PedidoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Pedido confirmar(UUID id) {
        Pedido pedido = buscarOuLancar(id);
        pedido.confirmar(); // regra de negócio na entidade
        return repositoryPort.salvar(pedido);
    }

    @Override
    public Pedido cancelar(UUID id) {
        Pedido pedido = buscarOuLancar(id);
        pedido.cancelar(); // regra de negócio na entidade
        return repositoryPort.salvar(pedido);
    }

    private Pedido buscarOuLancar(UUID id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id.toString()));
    }
}
