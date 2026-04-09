package com.empresa.pedidos.adapter.in.web;

import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.application.port.in.AtualizarStatusPedidoUseCase;
import com.empresa.pedidos.application.port.in.ConsultarPedidoUseCase;
import com.empresa.pedidos.application.port.in.CriarPedidoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ADAPTER DE ENTRADA — Controller REST.
 *
 * Responsabilidades:
 *  - Receber a requisição HTTP
 *  - Validar o formato dos dados (@Valid)
 *  - Converter Request -> Command
 *  - Chamar o UseCase (Port In) — nunca a implementação concreta
 *  - Converter o resultado (Pedido) -> Response e retornar HTTP
 *
 * NÃO contém regra de negócio. Se você se pegar escrevendo
 * um if de negócio aqui, mova para o domain ou application.
 */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final CriarPedidoUseCase criarPedidoUseCase;
    private final ConsultarPedidoUseCase consultarPedidoUseCase;
    private final AtualizarStatusPedidoUseCase atualizarStatusPedidoUseCase;

    public PedidoController(CriarPedidoUseCase criarPedidoUseCase,
                             ConsultarPedidoUseCase consultarPedidoUseCase,
                             AtualizarStatusPedidoUseCase atualizarStatusPedidoUseCase) {
        this.criarPedidoUseCase = criarPedidoUseCase;
        this.consultarPedidoUseCase = consultarPedidoUseCase;
        this.atualizarStatusPedidoUseCase = atualizarStatusPedidoUseCase;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        // Request -> Command (adapter converte, domain valida regras)
        var command = new CriarPedidoUseCase.Command(
                request.descricao(),
                request.valor(),
                request.cep()
        );

        Pedido pedido = criarPedidoUseCase.executar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.de(pedido));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable UUID id) {
        Pedido pedido = consultarPedidoUseCase.buscarPorId(id);
        return ResponseEntity.ok(PedidoResponse.de(pedido));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarTodos() {
        List<PedidoResponse> responses = consultarPedidoUseCase.listarTodos()
                .stream()
                .map(PedidoResponse::de)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponse> confirmar(@PathVariable UUID id) {
        Pedido pedido = atualizarStatusPedidoUseCase.confirmar(id);
        return ResponseEntity.ok(PedidoResponse.de(pedido));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable UUID id) {
        Pedido pedido = atualizarStatusPedidoUseCase.cancelar(id);
        return ResponseEntity.ok(PedidoResponse.de(pedido));
    }
}
