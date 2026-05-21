package com.titu.core.service;

import com.titu.core.model.Evento;
import com.titu.core.repository.EventoRepository;
import com.titu.core.repository.TipoEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final TipoEventoRepository tipoEventoRepository;
    private final FechamentoService fechamentoService;

    public List<Evento> listarTodos() {
        return eventoRepository.findAllByOrderByDataEventoDesc();
    }

    @Transactional
    public void salvar(Evento evento) {
        // --- A TRAVA DE SEGURANÇA ---
        int mes = evento.getDataEvento().getMonthValue();
        int ano = evento.getDataEvento().getYear();

        if (fechamentoService.isMesTrancado(mes, ano)) {
            throw new RuntimeException("Operação bloqueada! O mês financeiro desta festa já foi fechado.");
        }
        // ----------------------------

        // Blindagem contra NullPointer
        if (evento.getReceitaBar() == null) evento.setReceitaBar(java.math.BigDecimal.ZERO);

        // Custos Diretos
        if (evento.getCustoProblemas() == null) evento.setCustoProblemas(java.math.BigDecimal.ZERO);
        if (evento.getCustoDiarias() == null) evento.setCustoDiarias(java.math.BigDecimal.ZERO);
        if (evento.getCustoPromoters() == null) evento.setCustoPromoters(java.math.BigDecimal.ZERO);
        if (evento.getCustoDjPagode() == null) evento.setCustoDjPagode(java.math.BigDecimal.ZERO);
        if (evento.getCustoSeguranca() == null) evento.setCustoSeguranca(java.math.BigDecimal.ZERO);
        if (evento.getCustoSom() == null) evento.setCustoSom(java.math.BigDecimal.ZERO);

        // Provisões
        if (evento.getProvisaoCustoBar() == null) evento.setProvisaoCustoBar(java.math.BigDecimal.ZERO);
        if (evento.getProvisaoSocios() == null) evento.setProvisaoSocios(java.math.BigDecimal.ZERO);
        if (evento.getProvisaoDecoracao() == null) evento.setProvisaoDecoracao(java.math.BigDecimal.ZERO);
        if (evento.getProvisaoTaxa() == null) evento.setProvisaoTaxa(java.math.BigDecimal.ZERO);

        eventoRepository.save(evento);
    }

    @Transactional
    public void excluir(Long id) {
        // Primeiro acha o evento para descobrir a data dele
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));

        // --- A TRAVA DE SEGURANÇA ---
        int mes = evento.getDataEvento().getMonthValue();
        int ano = evento.getDataEvento().getYear();

        if (fechamentoService.isMesTrancado(mes, ano)) {
            throw new RuntimeException("Operação bloqueada! Você não pode excluir festas de um mês já fechado.");
        }
        // ----------------------------

        eventoRepository.deleteById(id);
    }

    // Tradutor automático de dias do Java (Inglês) para o nosso padrão (Português)
    private String traduzirDiaDaSemana(java.time.DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Segunda";
            case TUESDAY -> "Terça";
            case WEDNESDAY -> "Quarta";
            case THURSDAY -> "Quinta";
            case FRIDAY -> "Sexta";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    @Transactional
    public void gerarMesCompleto(int ano, int mes) {
        // A Mágica do Java: YearMonth calcula dias, bissextos, 30 ou 31 sozinho!
        java.time.YearMonth anoMes = java.time.YearMonth.of(ano, mes);
        int quantidadeDias = anoMes.lengthOfMonth();

        for (int dia = 1; dia <= quantidadeDias; dia++) {
            java.time.LocalDate dataDaVez = anoMes.atDay(dia);

            // Só cria a linha se não existir nada cadastrado naquele dia
            if (!eventoRepository.existsByDataEvento(dataDaVez)) {
                Evento novoEvento = new Evento();
                novoEvento.setDataEvento(dataDaVez);

                // NOVO: Descobre qual dia da semana é hoje e traduz pra Português
                String nomeDia = traduzirDiaDaSemana(dataDaVez.getDayOfWeek());

                // Busca o tipo de evento com o nome do dia (ex: "Sexta")
                com.titu.core.model.TipoEvento tipoDefault = tipoEventoRepository.findByNome(nomeDia)
                        .orElseGet(() -> tipoEventoRepository.save(new com.titu.core.model.TipoEvento(null, nomeDia, "#6c757d")));  novoEvento.setTipoEvento(tipoDefault);

                salvar(novoEvento);
            }
        }
    }

    @Transactional
    public void apagarMesCompleto(int ano, int mes) {
        eventoRepository.deletarPorMesEAno(ano, mes);
    }

    public List<Evento> listarPorMesEAno(int ano, int mes) {
        return eventoRepository.findByMesEAno(ano, mes);
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado com o ID: " + id));
    }

}