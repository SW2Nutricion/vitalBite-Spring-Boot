package com.vitalbite.documental.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAuditoriaRepository extends JpaRepository<AuditoriaEntity, Long> {
    @Query("""
        select a from AuditoriaEntity a
        where (:accion is null or a.accion = :accion)
          and (:resultado is null or a.resultado = :resultado)
          and (:sub is null or a.sub = :sub)
        order by a.fecha desc
        """)
    Page<AuditoriaEntity> search(@Param("accion") String accion,
                                 @Param("resultado") String resultado,
                                 @Param("sub") String sub, Pageable pageable);
}
