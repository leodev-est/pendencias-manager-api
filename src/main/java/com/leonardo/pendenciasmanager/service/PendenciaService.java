package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.dto.Request.PendenciaRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.PageResponseDTO;
import com.leonardo.pendenciasmanager.dto.Response.PendenciaResponseDTO;
import com.leonardo.pendenciasmanager.entity.Pendencia;
import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.enums.Role;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.repository.PendenciaRepository;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import com.leonardo.pendenciasmanager.repository.specification.PendenciaSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PendenciaService {

    @Autowired
    private PendenciaRepository pendenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public PendenciaResponseDTO criar(PendenciaRequestDTO dto) {
        Usuario usuarioAutenticado = getUsuarioAutenticado();

        Pendencia pendencia = new Pendencia();
        pendencia.setTitulo(dto.getTitulo());
        pendencia.setDescricao(dto.getDescricao());
        pendencia.setStatus(dto.getStatus());
        pendencia.setDataVencimento(dto.getDataVencimento());
        pendencia.setPrioridade(dto.getPrioridade());
        pendencia.setOrigem(dto.getOrigem());
        pendencia.setResponsavel(usuarioAutenticado);

        return toResponseDTO(pendenciaRepository.save(pendencia));
    }

    public PageResponseDTO<PendenciaResponseDTO> listar(
            int page,
            int size,
            String sort,
            StatusPendencia status,
            String prioridade,
            String termo,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        Usuario usuarioAutenticado = getUsuarioAutenticado();
        validarParametrosDeListagem(page, size, dataInicio, dataFim);
        Pageable pageable = criarPageable(page, size, sort);
        Specification<Pendencia> specification = isAdmin(usuarioAutenticado)
                ? Specification.where(PendenciaSpecification.comStatus(status))
                .and(PendenciaSpecification.comPrioridade(prioridade))
                .and(PendenciaSpecification.comTermo(termo))
                .and(PendenciaSpecification.comDataInicio(dataInicio))
                .and(PendenciaSpecification.comDataFim(dataFim))
                : Specification.where(PendenciaSpecification.doUsuario(usuarioAutenticado.getId()))
                .and(PendenciaSpecification.comStatus(status))
                .and(PendenciaSpecification.comPrioridade(prioridade))
                .and(PendenciaSpecification.comTermo(termo))
                .and(PendenciaSpecification.comDataInicio(dataInicio))
                .and(PendenciaSpecification.comDataFim(dataFim));

        Page<Pendencia> pendencias = pendenciaRepository.findAll(specification, pageable);

        PageResponseDTO<PendenciaResponseDTO> response = new PageResponseDTO<>();
        response.setContent(pendencias.getContent().stream().map(this::toResponseDTO).collect(Collectors.toList()));
        response.setPage(pendencias.getNumber());
        response.setSize(pendencias.getSize());
        response.setTotalElements(pendencias.getTotalElements());
        response.setTotalPages(pendencias.getTotalPages());
        response.setLast(pendencias.isLast());
        return response;
    }

    public PendenciaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarPendenciaDoUsuario(id));
    }

    public PendenciaResponseDTO atualizar(Long id, PendenciaRequestDTO dto) {
        Pendencia pendencia = buscarPendenciaDoUsuario(id);

        pendencia.setTitulo(dto.getTitulo());
        pendencia.setDescricao(dto.getDescricao());
        pendencia.setStatus(dto.getStatus());
        pendencia.setDataVencimento(dto.getDataVencimento());
        pendencia.setPrioridade(dto.getPrioridade());
        pendencia.setOrigem(dto.getOrigem());

        return toResponseDTO(pendenciaRepository.save(pendencia));
    }

    public void deletar(Long id) {
        pendenciaRepository.delete(buscarPendenciaDoUsuario(id));
    }

    public List<PendenciaResponseDTO> listarPorStatus(StatusPendencia status) {
        Usuario usuarioAutenticado = getUsuarioAutenticado();

        List<Pendencia> pendencias = isAdmin(usuarioAutenticado)
                ? pendenciaRepository.findAll(
                Specification.where(PendenciaSpecification.comStatus(status))
        )
                : pendenciaRepository.findByResponsavelIdAndStatus(usuarioAutenticado.getId(), status);

        return pendencias
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PendenciaResponseDTO> listarVencidas() {
        Usuario usuarioAutenticado = getUsuarioAutenticado();
        LocalDate hoje = LocalDate.now();

        List<Pendencia> pendencias = isAdmin(usuarioAutenticado)
                ? pendenciaRepository.findAll(
                Specification.where(PendenciaSpecification.comDataFim(hoje.minusDays(1)))
                        .and((root, query, criteriaBuilder) ->
                                criteriaBuilder.notEqual(root.get("status"), StatusPendencia.CONCLUIDA))
        )
                : pendenciaRepository.findByResponsavelIdAndDataVencimentoBeforeAndStatusNot(
                        usuarioAutenticado.getId(),
                        hoje,
                        StatusPendencia.CONCLUIDA
                );

        return pendencias
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PendenciaResponseDTO> listarProximos7Dias() {
        Usuario usuarioAutenticado = getUsuarioAutenticado();
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(7);

        List<Pendencia> pendencias = isAdmin(usuarioAutenticado)
                ? pendenciaRepository.findAll(
                Specification.where(PendenciaSpecification.comDataInicio(hoje))
                        .and(PendenciaSpecification.comDataFim(limite))
        )
                : pendenciaRepository.findByResponsavelIdAndDataVencimentoBetween(usuarioAutenticado.getId(), hoje, limite);

        return pendencias
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Usuario nao autenticado.");
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado nao encontrado."));
    }

    private Pendencia buscarPendenciaDoUsuario(Long id) {
        Usuario usuarioAutenticado = getUsuarioAutenticado();
        Pendencia pendencia = pendenciaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pendencia nao encontrada."));

        if (!isAdmin(usuarioAutenticado)
                && (pendencia.getResponsavel() == null || !usuarioAutenticado.getId().equals(pendencia.getResponsavel().getId()))) {
            throw new AccessDeniedException("Acesso negado a esta pendencia.");
        }

        return pendencia;
    }

    private boolean isAdmin(Usuario usuario) {
        return usuario.getRole() == Role.ADMIN;
    }

    private void validarParametrosDeListagem(int page, int size, LocalDate dataInicio, LocalDate dataFim) {
        if (page < 0) {
            throw new BusinessException("O parametro page deve ser maior ou igual a zero.");
        }

        if (size <= 0) {
            throw new BusinessException("O parametro size deve ser maior que zero.");
        }

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new BusinessException("dataInicio nao pode ser maior que dataFim.");
        }
    }

    private Pageable criarPageable(int page, int size, String sort) {
        String sortParam = (sort == null || sort.isBlank()) ? "createdAt,desc" : sort;
        String[] sortParts = sortParam.split(",");

        if (sortParts.length == 0 || sortParts.length > 2 || sortParts[0].isBlank()) {
            throw new BusinessException("Parametro sort invalido.");
        }

        String property = mapearCampoOrdenacao(sortParts[0].trim());
        Sort.Direction direction = Sort.Direction.DESC;

        if (sortParts.length == 2 && !sortParts[1].isBlank()) {
            try {
                direction = Sort.Direction.fromString(sortParts[1].trim());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException("Direcao de ordenacao invalida.");
            }
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    private String mapearCampoOrdenacao(String campo) {
        return switch (campo) {
            case "dataLimite", "dataVencimento" -> "dataVencimento";
            case "status" -> "status";
            case "titulo" -> "titulo";
            case "dataCriacao", "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            default -> throw new BusinessException("Campo de ordenacao invalido.");
        };
    }

    private PendenciaResponseDTO toResponseDTO(Pendencia pendencia) {
        PendenciaResponseDTO dto = new PendenciaResponseDTO();
        dto.setId(pendencia.getId());
        dto.setTitulo(pendencia.getTitulo());
        dto.setDescricao(pendencia.getDescricao());
        dto.setStatus(pendencia.getStatus());
        dto.setDataVencimento(pendencia.getDataVencimento());
        dto.setPrioridade(pendencia.getPrioridade());
        dto.setOrigem(pendencia.getOrigem());
        dto.setCreatedAt(pendencia.getCreatedAt());
        dto.setUpdatedAt(pendencia.getUpdatedAt());
        dto.setResponsavelId(pendencia.getResponsavel().getId());
        dto.setResponsavelNome(pendencia.getResponsavel().getNome());
        return dto;
    }
}
