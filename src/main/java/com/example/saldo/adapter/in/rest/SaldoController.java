package com.example.saldo.adapter.in.rest;

import com.example.saldo.core.port.in.BuscarSaldoHttpPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador de ENTRADA — HTTP/REST.
 *
 * Responsabilidades:
 *  1. Receber a requisição HTTP
 *  2. Extrair parâmetros
 *  3. Acionar o core via BuscarSaldoHttpPort
 *  4. Converter Saldo → SaldoResponseDto e retornar
 *
 * Conhece apenas BuscarSaldoHttpPort — o contrato que o core
 * declarou para recebimento de consultas via protocolo de
 * requisição-resposta. Não sabe nada sobre a implementação
 * concreta nem sobre outros contratos do core.
 *
 * Se amanhã o REST for complementado por gRPC, basta criar um
 * novo adapter que também injete BuscarSaldoHttpPort (ou uma
 * BuscarSaldoGrpcPort equivalente) — o core não muda.
 *
 * GET /saldos/{id}            → busca saldo por ID
 * GET /saldos?contaId={valor} → lista saldos de uma conta
 */
@RestController
@RequestMapping("/saldos")
public class SaldoController {

    private final BuscarSaldoHttpPort buscarSaldoHttpPort;

    public SaldoController(BuscarSaldoHttpPort buscarSaldoHttpPort) {
        this.buscarSaldoHttpPort = buscarSaldoHttpPort;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaldoResponseDto> buscarPorId(@PathVariable Long id) {
        SaldoResponseDto response = SaldoResponseDto.from(buscarSaldoHttpPort.buscarPorId(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SaldoResponseDto>> listarPorContaId(@RequestParam String contaId) {
        List<SaldoResponseDto> response = buscarSaldoHttpPort.listarPorContaId(contaId)
                .stream()
                .map(SaldoResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
