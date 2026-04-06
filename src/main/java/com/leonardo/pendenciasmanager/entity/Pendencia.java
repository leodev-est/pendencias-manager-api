package com.leonardo.pendenciasmanager.entity;

import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Pendencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusPendencia status;

    private LocalDate dataVencimento;

    private String prioridade;

    private String origem;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario responsavel;
}