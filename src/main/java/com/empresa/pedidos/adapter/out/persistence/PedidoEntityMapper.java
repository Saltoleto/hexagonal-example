package com.empresa.pedidos.adapter.out.persistence;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import org.springframework.stereotype.Component;

/**
 * MAPPER — PedidoEntity <-> Pedido (dominio).
 *
 * Converte entre a representacao de infraestrutura (JPA Entity)
 * e o modelo de dominio puro.
 *
 * Poderia ser gerado pelo MapStruct — mantido manual aqui para
 * tornar o mapeamento explicito e didatico.
 *
 * REGRA: este mapper mora no adapter, nunca no domain.
 */
@Component
public class PedidoEntityMapper {

    public PedidoEntity paraEntity(Pedido pedido) {
        Endereco end = pedido.getEndereco();
        return new PedidoEntity(
                pedido.getId(),
                pedido.getDescricao(),
                pedido.getValor(),
                pedido.getStatus(),
                end != null ? end.getCep()        : null,
                end != null ? end.getLogradouro() : null,
                end != null ? end.getCidade()     : null,
                pedido.getCriadoEm(),
                pedido.getAtualizadoEm()
        );
    }

    public Pedido paraDominio(PedidoEntity entity) {
        Endereco endereco = new Endereco(
                entity.getCep(),
                entity.getLogradouro(),
                entity.getCidade()
        );
        return Pedido.reconstituir(
                entity.getId(),
                entity.getDescricao(),
                entity.getValor(),
                entity.getStatus(),
                endereco,
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
