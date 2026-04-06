package com.leonardo.pendenciasmanager.dto;

import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PendenciaRequestDTO {

    @NotBlank
    private String titulo;

    private String descricao;

    @NotNull
    private StatusPendencia status;

    @NotNull
    private LocalDate dataVencimento;

    private String prioridade;

    private String origem;

    @NotNull
    private Long responsavelId;
}