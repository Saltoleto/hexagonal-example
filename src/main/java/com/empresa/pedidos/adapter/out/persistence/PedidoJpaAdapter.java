package com.empresa.pedidos.adapter.out.persistence;

import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.application.port.out.PedidoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ADAPTER DE SAIDA — Persistencia JPA.
 *
 * Implementa PedidoRepositoryPort (contrato definido pelo dominio).
 * O dominio nao sabe que este adapter existe — so conhece a interface.
 *
 * Fluxo:
 *   Pedido (dominio) -> mapper -> PedidoEntity -> JpaRepository -> banco
 *   banco -> JpaRepository -> PedidoEntity -> mapper -> Pedido (dominio)
 */
@Component
public class PedidoJpaAdapter implements PedidoRepositoryPort {

    private final PedidoJpaRepository jpaRepository;
    private final PedidoEntityMapper mapper;

    public PedidoJpaAdapter(PedidoJpaRepository jpaRepository, PedidoEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        PedidoEntity entity = mapper.paraEntity(pedido);
        PedidoEntity salvo = jpaRepository.save(entity);
        return mapper.paraDominio(salvo);
    }

    @Override
    public Optional<Pedido> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public List<Pedido> buscarTodos() {
        return jpaRepository.findAll().stream()
                .map(mapper::paraDominio)
                .toList();
    }
}
