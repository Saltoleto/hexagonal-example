package com.example.saldo.core.port.out;

import com.example.saldo.core.model.Saldo;

import java.util.List;
import java.util.Optional;

/**
 * Porta de SAÍDA (driven port).
 * Define o contrato que a camada de domínio/aplicação usa para persistência.
 * O domínio não conhece MySQL, JPA ou qualquer detalhe de infraestrutura.
 */
public interface SaldoRepositoryPort {

    /**
     * Salva um saldo no repositório.
     *
     * @param saldo entidade de domínio
     * @return saldo persistido com ID
     */
    Saldo salvar(Saldo saldo);

    /**
     * Busca saldo por ID.
     */
    Optional<Saldo> buscarPorId(Long id);

    /**
     * Lista todos os saldos de uma conta.
     */
    List<Saldo> listarPorContaId(String contaId);
}
