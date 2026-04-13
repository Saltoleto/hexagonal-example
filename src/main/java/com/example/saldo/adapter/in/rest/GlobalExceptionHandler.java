package com.example.saldo.adapter.in.rest;

import com.example.saldo.core.usecase.SaldoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Traduz exceções de domínio para respostas HTTP.
 * O core lança exceções semânticas; o adapter decide o status code.
 * Isso preserva a barreira: o domínio nunca conhece HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SaldoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNaoEncontrado(SaldoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "status", 404,
            "erro", "Não encontrado",
            "mensagem", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "status", 400,
            "erro", "Requisição inválida",
            "mensagem", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
