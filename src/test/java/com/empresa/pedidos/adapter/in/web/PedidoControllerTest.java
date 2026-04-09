package com.empresa.pedidos.adapter.in.web;

import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoNaoEncontradoException;
import com.empresa.pedidos.application.port.in.AtualizarStatusPedidoUseCase;
import com.empresa.pedidos.application.port.in.ConsultarPedidoUseCase;
import com.empresa.pedidos.application.port.in.CriarPedidoUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TESTE DO CONTROLLER — @WebMvcTest.
 *
 * Carrega apenas a camada web (sem JPA, sem banco).
 * Testa serialization/deserialization, validacoes HTTP e status codes.
 *
 * Use cases sao mockados — nao testamos logica de negocio aqui.
 */
@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CriarPedidoUseCase criarPedidoUseCase;

    @MockBean
    private ConsultarPedidoUseCase consultarPedidoUseCase;

    @MockBean
    private AtualizarStatusPedidoUseCase atualizarStatusPedidoUseCase;

    @Test
    void deveCriarPedidoERetornar201() throws Exception {
        var pedidoFake = Pedido.criar("Monitor", BigDecimal.valueOf(2500), Endereco.vazio());
        when(criarPedidoUseCase.executar(any())).thenReturn(pedidoFake);

        var body = new PedidoRequest("Monitor", BigDecimal.valueOf(2500), null);

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Monitor"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveRetornar400ParaDescricaoVazia() throws Exception {
        var body = new PedidoRequest("", BigDecimal.valueOf(100), null);

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.descricao").exists());
    }

    @Test
    void deveRetornar404QuandoPedidoNaoEncontrado() throws Exception {
        var id = java.util.UUID.randomUUID();
        when(consultarPedidoUseCase.buscarPorId(id))
                .thenThrow(new PedidoNaoEncontradoException(id.toString()));

        mockMvc.perform(get("/api/v1/pedidos/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());
    }
}
