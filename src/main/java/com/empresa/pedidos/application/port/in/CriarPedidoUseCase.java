package com.empresa.pedidos.application.port.in;

import com.empresa.pedidos.domain.model.Pedido;

import java.math.BigDecimal;

/**
 * PORT DE ENTRADA (Driving Port)
 *
 * Define o contrato da operação de criação de pedido.
 * O Controller conhece esta interface — nunca a implementação concreta.
 *
 * O record Command encapsula os dados de entrada já validados.
 */
public interface CriarPedidoUseCase {

    Pedido executar(Command command);

    record Command(String descricao, BigDecimal valor, String cep) {

        public Command {
            if (descricao == null || descricao.isBlank()) {
                throw new IllegalArgumentException("Descrição obrigatória");
            }
            if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Valor deve ser positivo");
            }
        }
    }
}
