package com.empresa.pedidos.application.usecase;

import com.empresa.pedidos.application.PedidoRepositoryPort;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * USE CASE — Atualizar Status do Pedido.
 *
 * Orquestra: busca → delega transição ao domínio → persiste.
 * A REGRA ("só PENDENTE pode confirmar") vive em Pedido.confirmar().
 * Este use case não sabe — nem precisa saber — dessa restrição.
 */
@Service
@Transactional
public class AtualizarStatusPedidoService {

    private final PedidoRepositoryPort repositoryPort;

    public AtualizarStatusPedidoService(PedidoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    public Pedido confirmar(UUID id) {
        Pedido pedido = buscarOuLancar(id);
        pedido.confirmar();
        return repositoryPort.salvar(pedido);
    }

    public Pedido cancelar(UUID id) {
        Pedido pedido = buscarOuLancar(id);
        pedido.cancelar();
        return repositoryPort.salvar(pedido);
    }

    private Pedido buscarOuLancar(UUID id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id.toString()));
    }
}
