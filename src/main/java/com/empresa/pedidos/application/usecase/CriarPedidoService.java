package com.empresa.pedidos.application.usecase;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoDomainService;
import com.empresa.pedidos.application.port.in.CriarPedidoUseCase;
import com.empresa.pedidos.application.port.out.EnderecoServicePort;
import com.empresa.pedidos.application.port.out.PedidoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * USE CASE — Criar Pedido.
 *
 * Responsabilidade: ORQUESTRAR o fluxo de criação de um pedido.
 * Não contém regra de negócio — apenas coordena quem faz o quê e em que ordem.
 *
 * Fluxo:
 *   1. Delega validação de negócio ao PedidoDomainService  ← REGRA (domínio)
 *   2. Busca endereço via port out (API externa)            ← EFEITO COLATERAL
 *   3. Solicita criação da entidade ao domínio              ← DOMÍNIO cria
 *   4. Persiste via port out                                ← EFEITO COLATERAL
 *
 * Distinção importante:
 *   PedidoDomainService → sabe SE pode criar (regra de negócio)
 *   CriarPedidoService  → sabe COMO criar (fluxo, ports, ordem das chamadas)
 */
@Service
@Transactional
public class CriarPedidoService implements CriarPedidoUseCase {

    private final PedidoDomainService pedidoDomainService;
    private final PedidoRepositoryPort repositoryPort;
    private final EnderecoServicePort enderecoServicePort;

    public CriarPedidoService(PedidoDomainService pedidoDomainService,
                               PedidoRepositoryPort repositoryPort,
                               EnderecoServicePort enderecoServicePort) {
        this.pedidoDomainService = pedidoDomainService;
        this.repositoryPort = repositoryPort;
        this.enderecoServicePort = enderecoServicePort;
    }

    @Override
    public Pedido executar(Command command) {
        // 1. DOMÍNIO valida as regras de negócio (não o Use Case)
        pedidoDomainService.validarCriacao(command.descricao(), command.valor());

        // 2. Busca endereço na API externa via port out (efeito colateral)
        Endereco endereco = command.cep() != null
                ? enderecoServicePort.buscarPorCep(command.cep())
                : Endereco.vazio();

        // 3. DOMÍNIO cria a entidade
        Pedido pedido = Pedido.criar(command.descricao(), command.valor(), endereco);

        // 4. Persiste via port out (efeito colateral)
        return repositoryPort.salvar(pedido);
    }
}
