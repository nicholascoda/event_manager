package com.titu.core.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardMensalDTO {
    // Dinheiro das Festas
    private BigDecimal faturamentoBrutoEventos = BigDecimal.ZERO;
    private BigDecimal custosDiretosEventos = BigDecimal.ZERO;
    private BigDecimal lucroOperacionalEventos = BigDecimal.ZERO;

    // Boletos (Despesas da Aba 3)
    private BigDecimal totalDespesasFixas = BigDecimal.ZERO;

    // O que sobra no bolso do dono!
    private BigDecimal lucroLiquidoReal = BigDecimal.ZERO;

    // A lista mastigada de qual festa deu mais dinheiro
    private List<ResumoFestaDTO> rankingFestas = new ArrayList<>();
}