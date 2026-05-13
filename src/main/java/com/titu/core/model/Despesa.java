package com.titu.core.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "despesas")
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao; // Ex: Conta de Luz, iFood, Compra de Estoque

    // Aqui é a Categoria para o Dashboard ficar bonitão depois!
    @Column(nullable = false)
    private String categoria; // Ex: Infraestrutura, Alimentação, Impostos

    // Aqui entra o Fornecedor (O nosso Business Partner)
    // Deixamos nullable = true para permitir aquela ideia de "Despesa Avulsa" (sem fornecedor cadastrado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Cliente fornecedor;

    @Column(nullable = false)
    private java.math.BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDespesa status = StatusDespesa.PENDENTE;

    @Column(columnDefinition = "TEXT")
    private String observacao;
}