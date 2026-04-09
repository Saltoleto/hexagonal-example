package com.empresa.pedidos.application;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.port.in.CriarPedidoUseCase;
import com.empresa.pedidos.domain.port.out.EnderecoServicePort;
import com.empresa.pedidos.domain.port.out.PedidoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * APPLICATION SERVICE — Implementa CriarPedidoUseCase.
 *
 * Responsabilidade: orquestrar a criação de um pedido.
 *   1. Busca o endereço via API externa (port out)
 *   2. Cria a entidade de domínio
 *   3. Persiste via repositório (port out)
 *
 * NÃO contém regra de negócio — regras vivem no domain.
 * Pode usar @Service e @Transactional (Spring é detalhe de infra aqui).
 */
@Service
@Transactional
public class CriarPedidoService implements CriarPedidoUseCase {

    private final PedidoRepositoryPort repositoryPort;
    private final EnderecoServicePort enderecoServicePort;

    public CriarPedidoService(PedidoRepositoryPort repositoryPort,
                               EnderecoServicePort enderecoServicePort) {
        this.repositoryPort = repositoryPort;
        this.enderecoServicePort = enderecoServicePort;
    }

    @Override
    public Pedido executar(Command command) {
        // 1. Busca endereço na API externa (se CEP informado)
        Endereco endereco = command.cep() != null
                ? enderecoServicePort.buscarPorCep(command.cep())
                : Endereco.vazio();

        // 2. Cria a entidade — lógica de negócio dentro do domain
        Pedido pedido = Pedido.criar(command.descricao(), command.valor(), endereco);

        // 3. Persiste e retorna
        return repositoryPort.salvar(pedido);
    }
}
