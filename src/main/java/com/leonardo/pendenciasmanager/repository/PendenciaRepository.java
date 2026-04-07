package com.leonardo.pendenciasmanager.repository;

import com.leonardo.pendenciasmanager.entity.Pendencia;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PendenciaRepository extends JpaRepository<Pendencia, Long> {

    List<Pendencia> findByStatus(StatusPendencia status);

    List<Pendencia> findByResponsavelId(Long responsavelId);

    boolean existsByResponsavelId(Long responsavelId);

    List<Pendencia> findByDataVencimentoBeforeAndStatusNot(LocalDate data, StatusPendencia status);

    List<Pendencia> findByDataVencimentoBetween(LocalDate dataInicio, LocalDate dataFim);
}
