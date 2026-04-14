package com.example.saldo.core.port.in;

import com.example.saldo.core.model.Saldo;

import java.util.List;

/**
 * Porta de entrada para consulta de saldo via requisição HTTP.
 *
 * Declara formalmente que o core aceita acionamentos de consulta
 * via protocolo de requisição-resposta, sem saber se é REST,
 * gRPC ou qualquer outro protocolo.
 *
 * Implementada por: BuscarSaldoUseCaseImpl (core/usecase)
 * Chamada por:      SaldoController (adapter/in/rest)
 */
public interface BuscarSaldoHttpPort {

    /**
     * Busca um saldo pelo ID.
     */
    Saldo buscarPorId(Long id);

    /**
     * Lista todos os saldos de uma conta.
     */
    List<Saldo> listarPorContaId(String contaId);
}
