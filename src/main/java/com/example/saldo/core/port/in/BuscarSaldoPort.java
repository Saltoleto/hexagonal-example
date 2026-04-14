package com.example.saldo.core.port.in;

import com.example.saldo.core.model.Saldo;

import java.util.List;

/**
 * Porta de ENTRADA para consulta de saldos.
 *
 * Define o contrato pelo qual o mundo externo aciona o core
 * para recuperar saldos — sem saber se a requisição veio de
 * REST, gRPC ou qualquer outro protocolo.
 *
 * Implementada por: BuscarSaldoUseCaseImpl (core/usecase)
 * Chamada por:      SaldoController (adapter/in/rest)
 */
public interface BuscarSaldoPort {

    /**
     * Busca um saldo pelo ID.
     */
    Saldo buscarPorId(Long id);

    /**
     * Lista todos os saldos de uma conta.
     */
    List<Saldo> listarPorContaId(String contaId);
}
