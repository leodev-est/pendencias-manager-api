package com.leonardo.pendenciasmanager.repository;

import com.leonardo.pendenciasmanager.entity.Pendencia;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PendenciaRepository extends JpaRepository<Pendencia, Long> {

    @Query("select p from Pendencia p where p.responsavel.id = :usuarioId")
    List<Pendencia> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    List<Pendencia> findByResponsavelId(Long responsavelId);

    List<Pendencia> findByResponsavelIdAndStatus(Long responsavelId, StatusPendencia status);

    boolean existsByResponsavelId(Long responsavelId);

    List<Pendencia> findByResponsavelIdAndDataVencimentoBeforeAndStatusNot(
            Long responsavelId,
            LocalDate data,
            StatusPendencia status
    );

    List<Pendencia> findByResponsavelIdAndDataVencimentoBetween(
            Long responsavelId,
            LocalDate dataInicio,
            LocalDate dataFim
    );
}
