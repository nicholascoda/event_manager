package com.titu.core.repository;

import com.titu.core.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TipoEventoRepository extends JpaRepository<TipoEvento, Long> {
    Optional<TipoEvento> findByNome(String nome);
    boolean existsByNome(String nome);
}