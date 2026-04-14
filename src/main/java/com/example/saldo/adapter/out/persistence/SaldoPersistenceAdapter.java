package com.example.saldo.adapter.out.persistence;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.port.out.SaldoRepositoryPort;
import com.example.saldo.adapter.out.persistence.entity.SaldoEntity;
import com.example.saldo.adapter.out.persistence.mapper.SaldoPersistenceMapper;
import com.example.saldo.adapter.out.persistence.repository.SaldoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de SAÍDA (driven adapter).
 * Implementa a porta SaldoRepositoryPort usando JPA + MySQL.
 * O domínio conhece apenas a interface (porta); nunca esta classe.
 */
@Component
public class SaldoPersistenceAdapter implements SaldoRepositoryPort {

    private final SaldoJpaRepository jpaRepository;
    private final SaldoPersistenceMapper mapper;

    public SaldoPersistenceAdapter(SaldoJpaRepository jpaRepository,
                                   SaldoPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Saldo salvar(Saldo saldo) {
        SaldoEntity entity = mapper.toEntity(saldo);
        SaldoEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Saldo> buscarPorId(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Saldo> listarPorContaId(String contaId) {
        return jpaRepository.findByContaId(contaId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
