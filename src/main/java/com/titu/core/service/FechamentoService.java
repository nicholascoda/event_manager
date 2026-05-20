package com.titu.core.service;

import com.titu.core.model.FechamentoMes;
import com.titu.core.repository.FechamentoMesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FechamentoService {

    private final FechamentoMesRepository fechamentoRepository;

    // Método que o botão do Dashboard vai acionar
    public void fecharCompetencia(int ano, int mes, String usuario) {
        FechamentoMes fechamento = fechamentoRepository.findByMesAndAno(mes, ano)
                .orElse(FechamentoMes.builder().mes(mes).ano(ano).build());

        fechamento.setFechado(true);
        fechamento.setDataFechamento(LocalDateTime.now());
        fechamento.setUsuarioResponsavel(usuario);

        fechamentoRepository.save(fechamento);
    }

    // O escudo que vamos usar nas outras abas
    public boolean isMesTrancado(int mes, int ano) {
        return fechamentoRepository.existsByMesAndAnoAndFechadoTrue(mes, ano);
    }
}