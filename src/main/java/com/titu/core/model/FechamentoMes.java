package com.titu.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fechamentos_mes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FechamentoMes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private LocalDateTime dataFechamento;

    @Column(nullable = false)
    private String usuarioResponsavel; // Para ele saber quem fez o fechamento

    @Column(nullable = false)
    private boolean fechado;
}