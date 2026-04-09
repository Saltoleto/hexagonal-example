package com.empresa.pedidos.adapter.in.web;

import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO DE SAÍDA — dados enviados na resposta HTTP.
 *
 * Vantagens de ter um DTO separado da entidade de domínio:
 *  - Controle total sobre o que é exposto (sem vazar campos internos)
 *  - A API pode evoluir independente do domínio
 *  - Facilita versionamento de API
 *
 * O factory method `de(Pedido)` centraliza a conversão — sem mapper externo
 * para casos simples como este.
 */
public record PedidoResponse(
        UUID id,
        String descricao,
        BigDecimal valor,
        StatusPedido status,
        String statusDescricao,
        EnderecoResponse endereco,
        LocalDateTime criadoEm
) {

    public static PedidoResponse de(Pedido pedido) {
        EnderecoResponse enderecoResponse = pedido.getEndereco() != null
                ? new EnderecoResponse(
                        pedido.getEndereco().getCep(),
                        pedido.getEndereco().getLogradouro(),
                        pedido.getEndereco().getCidade())
                : null;

        return new PedidoResponse(
                pedido.getId(),
                pedido.getDescricao(),
                pedido.getValor(),
                pedido.getStatus(),
                pedido.getStatus().getDescricao(),
                enderecoResponse,
                pedido.getCriadoEm()
        );
    }

    public record EnderecoResponse(String cep, String logradouro, String cidade) {}
}
