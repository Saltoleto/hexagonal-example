package com.empresa.pedidos.adapter.out.persistence;

import com.empresa.pedidos.application.PedidoRepositoryPort;
import com.empresa.pedidos.domain.model.Pedido;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ADAPTER DE SAÍDA — Persistência JPA.
 *
 * Implementa PedidoRepositoryPort (contrato da camada de aplicação).
 * Converte Pedido (domínio) ↔ PedidoEntity (JPA) via mapper.
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
        return mapper.paraDominio(jpaRepository.save(mapper.paraEntity(pedido)));
    }

    @Override
    public Optional<Pedido> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public List<Pedido> buscarTodos() {
        return jpaRepository.findAll().stream().map(mapper::paraDominio).toList();
    }
}
