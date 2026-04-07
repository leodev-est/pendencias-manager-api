package com.leonardo.pendenciasmanager.entity;

import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private LocalDateTime dataCriacao;

    private String prioridade;

    private String origem;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario responsavel;

    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
}
