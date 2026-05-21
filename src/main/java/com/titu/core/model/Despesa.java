package com.titu.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "despesas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Coluna: DATA (Na planilha não separa muito vencimento de pagamento,
    // mas a gente mantém a data de lançamento principal)
    @Column(nullable = false)
    private LocalDate dataLancamento;

    // Coluna: Nº DOCUMENTO (Opcional)
    private String numeroDocumento;

    // Coluna: DESCRIÇÃO (Para ele saber do que se trata)
    @Column(nullable = false)
    private String descricao;

    // Coluna: FORNECEDOR / BENEFICIÁRIO
    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Cliente fornecedor;

    // Coluna: FORMA PGTO
    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    // Coluna: CATEGORIA (Agora aponta pra tabela de categorias dinâmicas)
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Coluna: VALOR (R$)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    // Coluna: STATUS (Pago, Pendente...)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDespesa status;

    // Coluna: OBSERVAÇÕES
    @Column(columnDefinition = "TEXT")
    private String observacao;

    // Para controle interno de quando a baixa foi dada
    private LocalDate dataPagamento;
}