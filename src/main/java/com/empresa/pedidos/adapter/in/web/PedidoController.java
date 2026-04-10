package com.empresa.pedidos.adapter.in.web;

import com.empresa.pedidos.application.usecase.AtualizarStatusPedidoService;
import com.empresa.pedidos.application.usecase.ConsultarPedidoService;
import com.empresa.pedidos.application.usecase.CriarPedidoService;
import com.empresa.pedidos.domain.model.Pedido;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ADAPTER DE ENTRADA — Controller REST.
 *
 * Injeta os use cases diretamente (@Service), sem interface de port in.
 * Decisão documentada em DA-02 do README: o Spring garante desacoplamento
 * via injeção — a interface adicional seria overhead sem benefício prático
 * enquanto houver apenas um adapter de entrada.
 *
 * Responsabilidades:
 *   - Receber HTTP e validar formato (@Valid)
 *   - Converter Request em parâmetros para o use case
 *   - Converter resultado em Response e retornar HTTP
 */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final CriarPedidoService criarPedidoService;
    private final ConsultarPedidoService consultarPedidoService;
    private final AtualizarStatusPedidoService atualizarStatusPedidoService;

    public PedidoController(CriarPedidoService criarPedidoService,
                             ConsultarPedidoService consultarPedidoService,
                             AtualizarStatusPedidoService atualizarStatusPedidoService) {
        this.criarPedidoService = criarPedidoService;
        this.consultarPedidoService = consultarPedidoService;
        this.atualizarStatusPedidoService = atualizarStatusPedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        Pedido pedido = criarPedidoService.executar(
                request.descricao(), request.valor(), request.cep());
        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.de(pedido));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(PedidoResponse.de(consultarPedidoService.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarTodos() {
        List<PedidoResponse> responses = consultarPedidoService.listarTodos()
                .stream().map(PedidoResponse::de).toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponse> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(PedidoResponse.de(atualizarStatusPedidoService.confirmar(id)));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(PedidoResponse.de(atualizarStatusPedidoService.cancelar(id)));
    }
}
