package com.titu.core.service;

import com.titu.core.model.Evento;
import com.titu.core.repository.EventoRepository;
import com.titu.core.repository.TipoEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
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

        // Blindagem contra NullPointer ultra compacta
        if (evento.getReceitaBar() == null) {
            evento.setReceitaBar(BigDecimal.ZERO);
        }

        // Garante que a gaveta do JSON nunca seja nula
        if (evento.getCustosDetalhados() == null) {
            evento.setCustosDetalhados(new HashMap<>());
        }

        eventoRepository.save(evento);
    }

    @Transactional
    public void excluir(Long id) {
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
        java.time.YearMonth anoMes = java.time.YearMonth.of(ano, mes);
        int quantidadeDias = anoMes.lengthOfMonth();

        for (int dia = 1; dia <= quantidadeDias; dia++) {
            java.time.LocalDate dataDaVez = anoMes.atDay(dia);

            if (!eventoRepository.existsByDataEvento(dataDaVez)) {
                Evento novoEvento = Evento.builder()
                        .dataEvento(dataDaVez)
                        .receitaBar(BigDecimal.ZERO)
                        .custosDetalhados(new HashMap<>()) // Inicializa o JSON vazio para cada dia
                        .build();

                String nomeDia = traduzirDiaDaSemana(dataDaVez.getDayOfWeek());

                com.titu.core.model.TipoEvento tipoDefault = tipoEventoRepository.findByNome(nomeDia)
                        .orElseGet(() -> tipoEventoRepository.save(new com.titu.core.model.TipoEvento(null, nomeDia, "#6c757d")));

                novoEvento.setTipoEvento(tipoDefault);
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