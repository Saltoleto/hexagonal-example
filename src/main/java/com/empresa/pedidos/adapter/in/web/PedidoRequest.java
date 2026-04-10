package com.empresa.pedidos.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO DE ENTRADA — dados recebidos na requisição HTTP.
 *
 * Responsabilidades aqui:
 *  - Validação de formato (@Valid + Bean Validation)
 *  - NÃO contém lógica de negócio
 *  - NÃO é a entidade de domínio
 *
 * O Controller passa os campos diretamente ao use case (sem Command intermediário — ver DA-02 no README).
 */
public record PedidoRequest(

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 3, max = 255, message = "Descrição deve ter entre 3 e 255 caracteres")
        String descricao,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido. Formato esperado: 00000-000")
        String cep
) {
}
