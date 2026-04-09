package com.empresa.pedidos.application.usecase;

import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoNaoEncontradoException;
import com.empresa.pedidos.application.port.in.AtualizarStatusPedidoUseCase;
import com.empresa.pedidos.application.port.out.PedidoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * USE CASE — Atualizar Status do Pedido.
 *
 * Responsabilidade: ORQUESTRAR a mudança de status de um pedido.
 *
 * Fluxo de confirmar:
 *   1. Busca o pedido via port out          ← efeito colateral
 *   2. Delega a transição ao domínio        ← REGRA: pedido.confirmar()
 *   3. Persiste o estado atualizado         ← efeito colateral
 *
 * A REGRA "só PENDENTE pode ser confirmado" vive em Pedido.confirmar().
 * O Use Case não sabe — e não precisa saber — desta restrição.
 * Se a regra mudar, só o domínio muda.
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
        pedido.confirmar();                // domínio executa e valida a regra
        return repositoryPort.salvar(pedido);
    }

    @Override
    public Pedido cancelar(UUID id) {
        Pedido pedido = buscarOuLancar(id);
        pedido.cancelar();                 // domínio executa e valida a regra
        return repositoryPort.salvar(pedido);
    }

    private Pedido buscarOuLancar(UUID id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id.toString()));
    }
}
