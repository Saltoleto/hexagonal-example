package com.empresa.pedidos.adapter.in.web;

import com.empresa.pedidos.application.usecase.AtualizarStatusPedidoService;
import com.empresa.pedidos.application.usecase.ConsultarPedidoService;
import com.empresa.pedidos.application.usecase.CriarPedidoService;
import com.empresa.pedidos.domain.model.Endereco;
import com.empresa.pedidos.domain.model.Pedido;
import com.empresa.pedidos.domain.model.PedidoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TESTE DO CONTROLLER — @WebMvcTest.
 *
 * Carrega apenas a camada web. Testa serialização, validações HTTP e status codes.
 * Use cases são @MockBean — não há lógica de negócio aqui.
 */
@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CriarPedidoService criarPedidoService;
    @MockBean ConsultarPedidoService consultarPedidoService;
    @MockBean AtualizarStatusPedidoService atualizarStatusPedidoService;

    @Test
    void deveCriarPedidoERetornar201() throws Exception {
        var pedido = Pedido.criar("Monitor", BigDecimal.valueOf(2500), Endereco.vazio());
        when(criarPedidoService.executar(eq("Monitor"), eq(BigDecimal.valueOf(2500)), isNull()))
                .thenReturn(pedido);

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"descricao":"Monitor","valor":2500.00}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Monitor"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveRetornar400ParaDescricaoVazia() throws Exception {
        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"descricao":"","valor":100}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.descricao").exists());
    }

    @Test
    void deveRetornar404QuandoPedidoNaoEncontrado() throws Exception {
        UUID id = UUID.randomUUID();
        when(consultarPedidoService.buscarPorId(id))
                .thenThrow(new PedidoNaoEncontradoException(id.toString()));

        mockMvc.perform(get("/api/v1/pedidos/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").exists());
    }
}
