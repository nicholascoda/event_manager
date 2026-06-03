package com.titu.core.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colunas_diario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColunaDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeVisual; // O que o cliente lê. Ex: "DJ/Pagode"

    @Column(nullable = false, unique = true)
    private String chaveBanco; // O que o Java/JSON lê. Ex: "djPagode_hora"

    @Column(nullable = false)
    private String tipo; // As tags: "_prov", "_hora" ou "_fixo"
}