package com.example.saldo.adapter.out.persistence.mapper;

import com.example.saldo.core.model.Saldo;
import com.example.saldo.core.model.TipoSaldo;
import com.example.saldo.adapter.out.persistence.entity.SaldoEntity;
import com.example.saldo.adapter.out.persistence.entity.SaldoEntity.TipoSaldoJpa;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre a entidade de domínio e a entidade JPA.
 * Garante o isolamento total entre as duas camadas.
 * (Poderia ser substituído por MapStruct para projetos maiores)
 */
@Component
public class SaldoPersistenceMapper {

    public SaldoEntity toEntity(Saldo saldo) {
        SaldoEntity entity = new SaldoEntity();
        entity.setId(saldo.getId());
        entity.setContaId(saldo.getContaId());
        entity.setValor(saldo.getValor());
        entity.setMoeda(saldo.getMoeda());
        entity.setTipo(TipoSaldoJpa.valueOf(saldo.getTipo().name()));
        entity.setDataReferencia(saldo.getDataReferencia());
        entity.setDataProcessamento(saldo.getDataProcessamento());
        return entity;
    }

    public Saldo toDomain(SaldoEntity entity) {
        Saldo saldo = new Saldo();
        saldo.setId(entity.getId());
        saldo.setContaId(entity.getContaId());
        saldo.setValor(entity.getValor());
        saldo.setMoeda(entity.getMoeda());
        saldo.setTipo(TipoSaldo.valueOf(entity.getTipo().name()));
        saldo.setDataReferencia(entity.getDataReferencia());
        saldo.setDataProcessamento(entity.getDataProcessamento());
        return saldo;
    }
}
