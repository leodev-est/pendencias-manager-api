package com.leonardo.pendenciasmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.leonardo.pendenciasmanager.dto.Request.PendenciaRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.PendenciaResponseDTO;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.exception.GlobalExceptionHandler;
import com.leonardo.pendenciasmanager.service.PendenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PendenciaControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private PendenciaService pendenciaService;

    @InjectMocks
    private PendenciaController pendenciaController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(pendenciaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void criarDeveRetornarPendenciaCriada() throws Exception {
        PendenciaRequestDTO request = criarRequest();
        PendenciaResponseDTO response = criarResponse();

        when(pendenciaService.criar(any(PendenciaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/pendencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Corrigir bug"))
                .andExpect(jsonPath("$.responsavelNome").value("Maria"));
    }

    @Test
    void criarDeveRetornarBadRequestQuandoPayloadForInvalido() throws Exception {
        PendenciaRequestDTO request = new PendenciaRequestDTO();

        mockMvc.perform(post("/pendencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarDeveRetornarListaDePendencias() throws Exception {
        when(pendenciaService.listar()).thenReturn(List.of(criarResponse()));

        mockMvc.perform(get("/pendencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));
    }

    @Test
    void buscarPorIdDeveRetornarPendencia() throws Exception {
        when(pendenciaService.buscarPorId(1L)).thenReturn(criarResponse());

        mockMvc.perform(get("/pendencias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Corrigir bug"));
    }

    @Test
    void atualizarDeveRetornarPendenciaAtualizada() throws Exception {
        PendenciaRequestDTO request = criarRequest();
        PendenciaResponseDTO response = criarResponse();
        response.setTitulo("Atualizada");

        when(pendenciaService.atualizar(eq(1L), any(PendenciaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/pendencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Atualizada"));
    }

    @Test
    void deletarDeveRetornarOk() throws Exception {
        mockMvc.perform(delete("/pendencias/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorStatusDeveRetornarPendenciasFiltradas() throws Exception {
        when(pendenciaService.listarPorStatus(StatusPendencia.PENDENTE)).thenReturn(List.of(criarResponse()));

        mockMvc.perform(get("/pendencias/status").param("status", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));
    }

    @Test
    void listarPorResponsavelDeveRetornarPendenciasDoResponsavel() throws Exception {
        when(pendenciaService.listarPorResponsavel(10L)).thenReturn(List.of(criarResponse()));

        mockMvc.perform(get("/pendencias/responsavel/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].responsavelId").value(10));
    }

    @Test
    void listarVencidasDeveRetornarPendencias() throws Exception {
        when(pendenciaService.listarVencidas()).thenReturn(List.of(criarResponse()));

        mockMvc.perform(get("/pendencias/vencidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listarProximos7DiasDeveRetornarPendencias() throws Exception {
        when(pendenciaService.listarProximos7Dias()).thenReturn(List.of(criarResponse()));

        mockMvc.perform(get("/pendencias/proximos-7-dias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Corrigir bug"));
    }

    @Test
    void endpointDeveRetornarBadRequestQuandoServiceLancarBusinessException() throws Exception {
        when(pendenciaService.buscarPorId(99L)).thenThrow(new BusinessException("Pendencia nao encontrada"));

        mockMvc.perform(get("/pendencias/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Pendencia nao encontrada"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deletarDeveRetornarBadRequestQuandoServiceLancarBusinessException() throws Exception {
        doThrow(new BusinessException("Pendencia nao encontrada")).when(pendenciaService).deletar(99L);

        mockMvc.perform(delete("/pendencias/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Pendencia nao encontrada"));
    }

    private PendenciaRequestDTO criarRequest() {
        PendenciaRequestDTO request = new PendenciaRequestDTO();
        request.setTitulo("Corrigir bug");
        request.setDescricao("Ajustar cadastro");
        request.setStatus(StatusPendencia.PENDENTE);
        request.setDataVencimento(LocalDate.of(2026, 4, 10));
        request.setPrioridade("Alta");
        request.setOrigem("Sistema");
        request.setResponsavelId(10L);
        return request;
    }

    private PendenciaResponseDTO criarResponse() {
        PendenciaResponseDTO response = new PendenciaResponseDTO();
        response.setId(1L);
        response.setTitulo("Corrigir bug");
        response.setDescricao("Ajustar cadastro");
        response.setStatus(StatusPendencia.PENDENTE);
        response.setDataVencimento(LocalDate.of(2026, 4, 10));
        response.setPrioridade("Alta");
        response.setOrigem("Sistema");
        response.setResponsavelId(10L);
        response.setResponsavelNome("Maria");
        return response;
    }
}
