package com.example.saldo.adapter.in.rest;

import com.example.saldo.core.port.in.BuscarSaldoUseCase;
import com.example.saldo.core.usecase.SaldoNaoEncontradoException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador de ENTRADA HTTP.
 * Recebe requisições REST e delega ao caso de uso — nunca contém lógica de negócio.
 *
 * GET /saldos/{id}            → busca saldo por ID
 * GET /saldos?contaId={id}    → lista saldos de uma conta
 */
@RestController
@RequestMapping("/saldos")
public class SaldoController {

    private final BuscarSaldoUseCase buscarSaldoUseCase;

    public SaldoController(BuscarSaldoUseCase buscarSaldoUseCase) {
        this.buscarSaldoUseCase = buscarSaldoUseCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaldoResponseDto> buscarPorId(@PathVariable Long id) {
        SaldoResponseDto response = SaldoResponseDto.from(buscarSaldoUseCase.buscarPorId(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SaldoResponseDto>> listarPorContaId(@RequestParam String contaId) {
        List<SaldoResponseDto> response = buscarSaldoUseCase.listarPorContaId(contaId)
                .stream()
                .map(SaldoResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
