package com.titu.core.service;

import com.titu.core.dto.DashboardMensalDTO;
import com.titu.core.dto.ResumoFestaDTO;
import com.titu.core.model.Evento;
import com.titu.core.repository.EventoRepository;
import com.titu.core.repository.DespesaRepository; // INJETADO
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventoRepository eventoRepository;
    private final DespesaRepository despesaRepository; // Conexão com a Aba 3 ativada!

    public DashboardMensalDTO processarDashboardDoMes(int ano, int mes) {
        DashboardMensalDTO dashboard = new DashboardMensalDTO();

        // 1. Busca todos os eventos do mês
        List<Evento> todosEventos = eventoRepository.findByMesEAno(ano, mes);

        // 2. Filtra os eventos fantasmas
        List<Evento> eventosReais = todosEventos.stream()
                .filter(e -> e.getReceitaBar().compareTo(BigDecimal.ZERO) > 0 || e.getTotalCustos().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        Map<String, ResumoFestaDTO> mapaFestas = new HashMap<>();

        for (Evento evento : eventosReais) {
            dashboard.setFaturamentoBrutoEventos(dashboard.getFaturamentoBrutoEventos().add(evento.getReceitaBar()));
            dashboard.setCustosDiretosEventos(dashboard.getCustosDiretosEventos().add(evento.getTotalCustos()));
            dashboard.setLucroOperacionalEventos(dashboard.getLucroOperacionalEventos().add(evento.getLucroDoDia()));

            String nomeFesta = evento.getTipoEvento().getNome();
            ResumoFestaDTO resumoFesta = mapaFestas.getOrDefault(nomeFesta, new ResumoFestaDTO());
            resumoFesta.setNomeFesta(nomeFesta);
            resumoFesta.setCor(evento.getTipoEvento().getCor());
            resumoFesta.adicionarValores(evento.getReceitaBar(), evento.getTotalCustos(), evento.getLucroDoDia());

            mapaFestas.put(nomeFesta, resumoFesta);
        }

        List<ResumoFestaDTO> ranking = mapaFestas.values().stream()
                .sorted((f1, f2) -> f2.getLucroTotal().compareTo(f1.getLucroTotal()))
                .collect(Collectors.toList());
        dashboard.setRankingFestas(ranking);

        // ==========================================
        // 3. O CRUZAMENTO REAL COM A ABA 3
        // ==========================================

        // Calcula o primeiro e o último dia do mês (O Java sabe se o mês tem 28, 30 ou 31 dias automático)
        java.time.LocalDate inicioDoMes = java.time.LocalDate.of(ano, mes, 1);
        java.time.LocalDate fimDoMes = java.time.YearMonth.of(ano, mes).atEndOfMonth();

        // Busca o total de boletos passando as datas exatas
        BigDecimal totalBoletos = despesaRepository.somarDespesasPorPeriodo(inicioDoMes, fimDoMes);

        // Se o banco retornar null (nenhuma despesa no mês), joga ZERO pra não quebrar a matemática
        dashboard.setTotalDespesasFixas(totalBoletos != null ? totalBoletos : BigDecimal.ZERO);

        // MATEMÁTICA REAL: Sobra do bar - Contas fixas da empresa
        dashboard.setLucroLiquidoReal(dashboard.getLucroOperacionalEventos().subtract(dashboard.getTotalDespesasFixas()));

        return dashboard;
    }
}