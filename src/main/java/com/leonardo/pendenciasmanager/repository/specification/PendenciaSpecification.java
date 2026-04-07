package com.leonardo.pendenciasmanager.repository.specification;

import com.leonardo.pendenciasmanager.entity.Pendencia;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class PendenciaSpecification {

    private PendenciaSpecification() {
    }

    public static Specification<Pendencia> doUsuario(Long usuarioId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("responsavel").get("id"), usuarioId);
    }

    public static Specification<Pendencia> comStatus(StatusPendencia status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Pendencia> comPrioridade(String prioridade) {
        return (root, query, criteriaBuilder) ->
                prioridade == null || prioridade.isBlank()
                        ? null
                        : criteriaBuilder.equal(criteriaBuilder.lower(root.get("prioridade")), prioridade.toLowerCase());
    }

    public static Specification<Pendencia> comTermo(String termo) {
        return (root, query, criteriaBuilder) -> {
            if (termo == null || termo.isBlank()) {
                return null;
            }

            String termoLike = "%" + termo.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("titulo")), termoLike),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("descricao")), termoLike)
            );
        };
    }

    public static Specification<Pendencia> comDataInicio(LocalDate dataInicio) {
        return (root, query, criteriaBuilder) ->
                dataInicio == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("dataVencimento"), dataInicio);
    }

    public static Specification<Pendencia> comDataFim(LocalDate dataFim) {
        return (root, query, criteriaBuilder) ->
                dataFim == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("dataVencimento"), dataFim);
    }
}
