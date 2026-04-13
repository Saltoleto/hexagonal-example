package com.example.saldo.core.port.in;

import com.example.saldo.core.model.Saldo;

import java.util.List;

/**
 * Porta de entrada para consulta de saldos.
 */
public interface BuscarSaldoUseCase {

    /**
     * Busca um saldo pelo ID.
     */
    Saldo buscarPorId(Long id);

    /**
     * Lista todos os saldos de uma conta.
     */
    List<Saldo> listarPorContaId(String contaId);
}
