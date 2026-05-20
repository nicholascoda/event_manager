package com.titu.core.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ResumoFestaDTO {
    private String nomeFesta;
    private String cor;
    private int quantidadeRealizada; // Quantas de fato aconteceram!
    private BigDecimal receitaTotal = BigDecimal.ZERO;
    private BigDecimal custoTotal = BigDecimal.ZERO;
    private BigDecimal lucroTotal = BigDecimal.ZERO;

    public void adicionarValores(BigDecimal receita, BigDecimal custo, BigDecimal lucro) {
        this.receitaTotal = this.receitaTotal.add(receita);
        this.custoTotal = this.custoTotal.add(custo);
        this.lucroTotal = this.lucroTotal.add(lucro);
        this.quantidadeRealizada++; // Conta +1 festa real
    }
}