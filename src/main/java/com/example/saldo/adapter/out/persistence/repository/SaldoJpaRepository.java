package com.example.saldo.adapter.out.persistence.repository;

import com.example.saldo.adapter.out.persistence.entity.SaldoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA.
 * Detalhe de infraestrutura — o domínio nunca enxerga esta interface.
 */
@Repository
public interface SaldoJpaRepository extends JpaRepository<SaldoEntity, Long> {

    List<SaldoEntity> findByContaId(String contaId);
}
