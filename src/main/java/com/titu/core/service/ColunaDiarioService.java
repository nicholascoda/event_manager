package com.titu.core.service;

import com.titu.core.model.ColunaDiario;
import com.titu.core.repository.ColunaDiarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColunaDiarioService {

    private final ColunaDiarioRepository repository;

    public List<ColunaDiario> listarTodas() {
        return repository.findAll();
    }

    public ColunaDiario salvar(ColunaDiario coluna) {
        // Se for uma coluna NOVA, garante que a chave não vai duplicar
        if (coluna.getId() == null && repository.existsByChaveBanco(coluna.getChaveBanco())) {
            throw new RuntimeException("Já existe uma coluna com essa chave.");
        }
        return repository.save(coluna);
    }

    public ColunaDiario buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Coluna não encontrada"));
    }

    // A MÁGICA: Gera as 11 colunas antigas automaticamente na primeira vez que rodar!
    @PostConstruct
    public void iniciarColunasPadrao() {
        if (repository.count() == 0) {
            salvar(new ColunaDiario(null, "Custo Bar", "custoBar_prov", "_prov"));
            salvar(new ColunaDiario(null, "Custo Fixo", "custoFixo_fixo", "_fixo"));
            salvar(new ColunaDiario(null, "Problemas", "problemas_hora", "_hora"));
            salvar(new ColunaDiario(null, "Diárias/Comp.", "diarias_hora", "_hora"));
            salvar(new ColunaDiario(null, "Promoters", "promoters_hora", "_hora"));
            salvar(new ColunaDiario(null, "DJ/Pagode", "djPagode_hora", "_hora"));
            salvar(new ColunaDiario(null, "Segurança", "seguranca_hora", "_hora"));
            salvar(new ColunaDiario(null, "Som", "som_hora", "_hora"));
            salvar(new ColunaDiario(null, "Sócios", "socios_prov", "_prov"));
            salvar(new ColunaDiario(null, "Decoração", "decoracao_prov", "_prov"));
            salvar(new ColunaDiario(null, "Taxa", "taxa_prov", "_prov"));
        }
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }
}