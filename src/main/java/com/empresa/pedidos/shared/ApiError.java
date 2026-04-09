package com.empresa.pedidos.shared;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ENVELOPE DE ERRO PADRONIZADO.
 *
 * Todas as respostas de erro da API seguem este formato.
 * Fica em 'shared' pois e usado pelo adapter.in.web (handler)
 * mas nao pertence a nenhuma camada especifica.
 *
 * Clientes da API podem contar com esta estrutura consistente.
 */
public record ApiError(
        int status,
        String erro,
        String mensagem,
        LocalDateTime timestamp,
        Map<String, String> campos  // preenchido apenas em erros de validacao
) {

    public static ApiError of(HttpStatus status, String mensagem) {
        return new ApiError(status.value(), status.getReasonPhrase(), mensagem,
                LocalDateTime.now(), null);
    }

    public static ApiError ofValidation(String mensagem, Map<String, String> campos) {
        return new ApiError(400, "Bad Request", mensagem,
                LocalDateTime.now(), campos);
    }
}
