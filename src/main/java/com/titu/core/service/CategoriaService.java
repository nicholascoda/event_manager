package com.titu.core.service;

import com.titu.core.model.Categoria;
import com.titu.core.model.TipoCategoria;
import com.titu.core.repository.CategoriaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
    }

    public void salvar(Categoria categoria) {
        if (categoria.getId() == null && categoriaRepository.existsByDescricao(categoria.getDescricao())) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome.");
        }
        categoriaRepository.save(categoria);
    }

    // =========================================================================
    // POPULANDO O BANCO INICIALMENTE COM A PLANILHA DO CLIENTE
    // =========================================================================
    @PostConstruct
    public void initCategoriasPadrao() {
        // Se a tabela estiver vazia, ele cria as categorias base da planilha!
        if (categoriaRepository.count() == 0) {
            salvar(new Categoria(null, "Custo Bar", TipoCategoria.PROVISAO));
            salvar(new Categoria(null, "Sócios", TipoCategoria.PROVISAO));
            salvar(new Categoria(null, "Decoração", TipoCategoria.PROVISAO));
            salvar(new Categoria(null, "Taxa", TipoCategoria.PROVISAO));

            salvar(new Categoria(null, "Aluguel", TipoCategoria.VARIAVEL));
            salvar(new Categoria(null, "Energia / Água / Internet", TipoCategoria.VARIAVEL));
            salvar(new Categoria(null, "Marketing / Eventos", TipoCategoria.VARIAVEL));
            salvar(new Categoria(null, "Manutenção", TipoCategoria.VARIAVEL));
            salvar(new Categoria(null, "Impostos / Taxas", TipoCategoria.VARIAVEL));
            salvar(new Categoria(null, "Folha Adicional", TipoCategoria.VARIAVEL));
            salvar(new Categoria(null, "Outros", TipoCategoria.VARIAVEL));
        }
    }
    @Transactional
    public void excluir(Long id) {
        // Verifica se existe antes de tentar deletar
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoria não encontrada.");
        }
        categoriaRepository.deleteById(id);
    }


}