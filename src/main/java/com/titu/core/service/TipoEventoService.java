package com.titu.core.service;

import com.titu.core.model.TipoEvento;
import com.titu.core.repository.TipoEventoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoEventoService {

    private final TipoEventoRepository repository;

    public List<TipoEvento> listarTodos() {
        return repository.findAll();
    }

    public void salvar(TipoEvento tipo) {
        // Se for um novo evento (ID nulo) e o nome já existir, ele barra.
        // Se já tiver ID, ele deixa passar para atualizar a cor.
        if (tipo.getId() == null && repository.existsByNome(tipo.getNome())) {
            throw new IllegalArgumentException("Esse tipo de evento já existe!");
        }
        repository.save(tipo);
    }

    @PostConstruct
    public void initTiposPadrao() {
        try {
            // Só injeta se a tabela estiver completamente vazia
            if (repository.count() == 0) {
                // Dias da Semana (Cor Cinza Muted para não chamar atenção)
                String corDia = "#6c757d";
                salvar(new TipoEvento(null, "Segunda", corDia));
                salvar(new TipoEvento(null, "Terça", corDia));
                salvar(new TipoEvento(null, "Quarta", corDia));
                salvar(new TipoEvento(null, "Quinta", corDia));
                salvar(new TipoEvento(null, "Sexta", corDia));
                salvar(new TipoEvento(null, "Sábado", corDia));
                salvar(new TipoEvento(null, "Domingo", corDia));

                // Festas (Cores chamativas!)
                salvar(new TipoEvento(null, "Sexta VIP", "#e83e8c")); // Rosa choque
                salvar(new TipoEvento(null, "Baile", "#dc3545"));     // Vermelho
                salvar(new TipoEvento(null, "Pagode", "#28a745"));    // Verde
            }
        } catch (Exception e) {
            // Se o banco estiver bugado, ele avisa no console mas NÃO trava o sistema!
            System.err.println("⚠️ AVISO: Erro ao injetar Tipos de Evento automáticos. " + e.getMessage());
        }
    }

    @Transactional
    public void excluir(Long id) {
        // Verifica se existe antes de tentar deletar
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Tipo de evento não encontrado.");
        }
        repository.deleteById(id);
    }

}