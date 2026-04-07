package com.leonardo.pendenciasmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leonardo.pendenciasmanager.dto.Request.UsuarioRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.UsuarioResponseDTO;
import com.leonardo.pendenciasmanager.enums.Role;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.exception.GlobalExceptionHandler;
import com.leonardo.pendenciasmanager.service.UsuarioService;
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
class UsuarioControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void criarDeveRetornarUsuarioCriado() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setNome("Leonardo");
        request.setEmail("leo@email.com");
        request.setSenha("123456");
        request.setCargo("Admin");
        request.setRole(Role.ADMIN);

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setNome("Leonardo");
        response.setEmail("leo@email.com");
        response.setCargo("Admin");
        response.setRole(Role.ADMIN);

        when(usuarioService.criar(any(UsuarioRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Leonardo"))
                .andExpect(jsonPath("$.email").value("leo@email.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void criarDeveRetornarBadRequestQuandoPayloadForInvalido() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setNome("");
        request.setEmail("email-invalido");
        request.setSenha("");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarDeveRetornarBadRequestQuandoServiceLancarBusinessException() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setNome("Leonardo");
        request.setEmail("leo@email.com");
        request.setSenha("123456");

        when(usuarioService.criar(any(UsuarioRequestDTO.class)))
                .thenThrow(new BusinessException("Email duplicado"));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Email duplicado"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listarDeveRetornarListaDeUsuarios() throws Exception {
        UsuarioResponseDTO usuario = new UsuarioResponseDTO();
        usuario.setId(1L);
        usuario.setNome("Ana");
        usuario.setEmail("ana@email.com");
        usuario.setCargo("Analista");
        usuario.setRole(Role.USER);

        when(usuarioService.listar()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Ana"))
                .andExpect(jsonPath("$[0].cargo").value("Analista"))
                .andExpect(jsonPath("$[0].role").value("USER"));
    }

    @Test
    void buscarPorIdDeveRetornarUsuario() throws Exception {
        UsuarioResponseDTO usuario = new UsuarioResponseDTO();
        usuario.setId(1L);
        usuario.setNome("Ana");
        usuario.setEmail("ana@email.com");
        usuario.setCargo("Analista");
        usuario.setRole(Role.USER);

        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    void atualizarDeveRetornarUsuarioAtualizado() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setNome("Ana Maria");
        request.setEmail("ana@email.com");
        request.setSenha("123456");
        request.setCargo("Senior");
        request.setRole(Role.ADMIN);

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setNome("Ana Maria");
        response.setEmail("ana@email.com");
        response.setCargo("Senior");
        response.setRole(Role.ADMIN);

        when(usuarioService.atualizar(eq(1L), any(UsuarioRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Maria"))
                .andExpect(jsonPath("$.cargo").value("Senior"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deletarDeveRetornarOk() throws Exception {
        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isOk());
    }

    @Test
    void endpointDeveRetornarBadRequestQuandoUsuarioNaoForEncontrado() throws Exception {
        when(usuarioService.buscarPorId(99L)).thenThrow(new BusinessException("Usuario nao encontrado"));

        mockMvc.perform(get("/usuarios/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Usuario nao encontrado"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deletarDeveRetornarBadRequestQuandoServiceLancarBusinessException() throws Exception {
        doThrow(new BusinessException("Usuario possui pendencias vinculadas")).when(usuarioService).deletar(1L);

        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Usuario possui pendencias vinculadas"));
    }
}
