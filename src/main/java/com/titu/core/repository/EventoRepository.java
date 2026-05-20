package com.titu.core.repository;

import com.titu.core.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findAllByOrderByDataEventoDesc();

    // NOVO: Verifica se já existe um evento naquela data para não duplicar
    boolean existsByDataEvento(LocalDate dataEvento);


    // NOVO: Para apagar um mês inteiro que foi gerado errado
    @Modifying
    @Query("DELETE FROM Evento e WHERE YEAR(e.dataEvento) = :ano AND MONTH(e.dataEvento) = :mes")
    void deletarPorMesEAno(@org.springframework.data.repository.query.Param("ano") int ano, @org.springframework.data.repository.query.Param("mes") int mes);

    // NOVO: Busca apenas os eventos de um mês/ano específico, ordenando do dia 1 ao 31
    @Query("SELECT e FROM Evento e WHERE YEAR(e.dataEvento) = :ano AND MONTH(e.dataEvento) = :mes ORDER BY e.dataEvento ASC")
    List<Evento> findByMesEAno(@org.springframework.data.repository.query.Param("ano") int ano, @org.springframework.data.repository.query.Param("mes") int mes);




}