package com.empresa.pedidos.application.usecase;

import com.empresa.pedidos.application.EnderecoServicePort;
import com.empresa.pedidos.application.PedidoRepositoryPort;
import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * USE CASE — Criar Pedido.
 *
 * Orquestra o fluxo sem conter regra de negócio:
 *   1. Busca endereço via port out (API externa)
 *   2. Delega criação e validação ao domínio (Pedido.criar)
 *   3. Persiste via port out
 *
 * Simplificação adotada (DA-02 no README): sem interface de port in.
 * O Controller injeta este @Service diretamente. O Spring garante o
 * desacoplamento via injeção de dependência — sem necessidade de interface
 * adicional para um único adapter de entrada.
 */
@Service
@Transactional
public class CriarPedidoService {

    private final PedidoRepositoryPort repositoryPort;
    private final EnderecoServicePort enderecoServicePort;

    public CriarPedidoService(PedidoRepositoryPort repositoryPort,
                               EnderecoServicePort enderecoServicePort) {
        this.repositoryPort = repositoryPort;
        this.enderecoServicePort = enderecoServicePort;
    }

    public Pedido executar(String descricao, BigDecimal valor, String cep) {
        Endereco endereco = cep != null
                ? enderecoServicePort.buscarPorCep(cep)
                : Endereco.vazio();

        Pedido pedido = Pedido.criar(descricao, valor, endereco);
        return repositoryPort.salvar(pedido);
    }
}
