package com.example.saldo.core.usecase;

/**
 * Exceção de domínio — sem anotações de framework.
 * O adapter REST é quem decide o status HTTP correspondente.
 */
public class SaldoNaoEncontradoException extends RuntimeException {

    public SaldoNaoEncontradoException(String message) {
        super(message);
    }
}
