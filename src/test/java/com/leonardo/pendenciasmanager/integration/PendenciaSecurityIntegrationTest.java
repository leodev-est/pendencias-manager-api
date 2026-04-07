package com.leonardo.pendenciasmanager.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.leonardo.pendenciasmanager.dto.Request.AuthLoginRequestDTO;
import com.leonardo.pendenciasmanager.entity.Pendencia;
import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.enums.Role;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import com.leonardo.pendenciasmanager.repository.PendenciaRepository;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PendenciaSecurityIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PendenciaRepository pendenciaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
        pendenciaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void loginDeveRetornarTokenQuandoCredenciaisForemValidas() throws Exception {
        criarUsuario("user@email.com", "123456", Role.USER);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarLoginRequest("user@email.com", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginDeveRetornarErroQuandoCredenciaisForemInvalidas() throws Exception {
        criarUsuario("user@email.com", "123456", Role.USER);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarLoginRequest("user@email.com", "senha-errada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais invalidas"));
    }

    @Test
    void acessoASegurancaDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/pendencias"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Token invalido ou expirado"));
    }

    @Test
    void acessoASegurancaDeveRetornar401ComTokenInvalido() throws Exception {
        mockMvc.perform(get("/pendencias")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Token invalido ou expirado"));
    }

    @Test
    void userDeveVerApenasAsPropriasPendencias() throws Exception {
        Usuario user = criarUsuario("user@email.com", "123456", Role.USER);
        Usuario outro = criarUsuario("outro@email.com", "123456", Role.USER);
        criarPendencia("Minha pendencia", StatusPendencia.PENDENTE, "Alta", user);
        criarPendencia("Pendencia de outro", StatusPendencia.PENDENTE, "Alta", outro);

        mockMvc.perform(get("/pendencias")
                        .header("Authorization", bearer(login("user@email.com", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Minha pendencia"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void userNaoDeveAcessarEditarOuDeletarPendenciaDeOutroUsuario() throws Exception {
        Usuario user = criarUsuario("user@email.com", "123456", Role.USER);
        Usuario outro = criarUsuario("outro@email.com", "123456", Role.USER);
        Pendencia pendenciaOutro = criarPendencia("Pendencia privada", StatusPendencia.PENDENTE, "Alta", outro);
        String token = login(user.getEmail(), "123456");

        mockMvc.perform(get("/pendencias/" + pendenciaOutro.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/pendencias/" + pendenciaOutro.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pendenciaRequestJson("Tentativa", StatusPendencia.EM_ANDAMENTO, "Alta")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/pendencias/" + pendenciaOutro.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeveVerTodasAsPendenciasEAcessarPendenciasDeOutrosUsuarios() throws Exception {
        Usuario admin = criarUsuario("admin@email.com", "123456", Role.ADMIN);
        Usuario user = criarUsuario("user@email.com", "123456", Role.USER);
        Usuario outro = criarUsuario("outro@email.com", "123456", Role.USER);
        Pendencia pendenciaUser = criarPendencia("Pendencia user", StatusPendencia.PENDENTE, "Alta", user);
        criarPendencia("Pendencia outro", StatusPendencia.EM_ANDAMENTO, "Baixa", outro);
        String token = login(admin.getEmail(), "123456");

        mockMvc.perform(get("/pendencias")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/pendencias/" + pendenciaUser.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Pendencia user"))
                .andExpect(jsonPath("$.responsavelNome").value("user"));
    }

    @Test
    void crudPrincipalDePendenciaDeveFuncionarParaUsuarioAutenticado() throws Exception {
        criarUsuario("user@email.com", "123456", Role.USER);
        String token = login("user@email.com", "123456");

        MvcResult createResult = mockMvc.perform(post("/pendencias")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pendenciaRequestJson("Nova pendencia", StatusPendencia.PENDENTE, "Alta")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Nova pendencia"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/pendencias/" + id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(put("/pendencias/" + id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pendenciaRequestJson("Pendencia atualizada", StatusPendencia.EM_ANDAMENTO, "Media")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Pendencia atualizada"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));

        mockMvc.perform(delete("/pendencias/" + id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pendencias/" + id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listagemDeveRespeitarPaginacaoFiltrosEOrdenacao() throws Exception {
        Usuario user = criarUsuario("user@email.com", "123456", Role.USER);
        criarPendencia("Zeta", StatusPendencia.PENDENTE, "Alta", user);
        criarPendencia("Alpha", StatusPendencia.PENDENTE, "Alta", user);
        criarPendencia("Concluida", StatusPendencia.CONCLUIDA, "Baixa", user);
        String token = login("user@email.com", "123456");

        mockMvc.perform(get("/pendencias")
                        .header("Authorization", bearer(token))
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "titulo,asc")
                        .param("status", "PENDENTE")
                        .param("prioridade", "Alta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Alpha"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    private Usuario criarUsuario(String email, String senha, Role role) {
        Usuario usuario = new Usuario();
        usuario.setNome(email.split("@")[0]);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setCargo(role.name());
        usuario.setRole(role);
        return usuarioRepository.save(usuario);
    }

    private Pendencia criarPendencia(String titulo, StatusPendencia status, String prioridade, Usuario responsavel) {
        Pendencia pendencia = new Pendencia();
        pendencia.setTitulo(titulo);
        pendencia.setDescricao("Descricao de " + titulo);
        pendencia.setStatus(status);
        pendencia.setDataVencimento(LocalDate.of(2026, 4, 30));
        pendencia.setPrioridade(prioridade);
        pendencia.setOrigem("Teste");
        pendencia.setResponsavel(responsavel);
        return pendenciaRepository.save(pendencia);
    }

    private AuthLoginRequestDTO criarLoginRequest(String email, String senha) {
        AuthLoginRequestDTO request = new AuthLoginRequestDTO();
        request.setEmail(email);
        request.setSenha(senha);
        return request;
    }

    private String login(String email, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarLoginRequest(email, senha))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String pendenciaRequestJson(String titulo, StatusPendencia status, String prioridade) throws Exception {
        JsonNode payload = objectMapper.createObjectNode()
                .put("titulo", titulo)
                .put("descricao", "Descricao atualizada")
                .put("status", status.name())
                .put("dataVencimento", "2026-04-30")
                .put("prioridade", prioridade)
                .put("origem", "Teste");
        return objectMapper.writeValueAsString(payload);
    }
}
