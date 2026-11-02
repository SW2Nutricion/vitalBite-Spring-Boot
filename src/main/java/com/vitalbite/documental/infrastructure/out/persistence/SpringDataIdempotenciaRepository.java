package com.vitalbite.documental.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface SpringDataIdempotenciaRepository
        extends JpaRepository<IdempotenciaEntity, String> {
    @Modifying
    @Query(value = """
            INSERT INTO idempotencia (clave, hash_solicitud, creado_en)
            VALUES (:key, :hash, CURRENT_TIMESTAMP)
            ON CONFLICT (clave) DO NOTHING
            """, nativeQuery = true)
    int insertReservation(@Param("key") String key, @Param("hash") String hash);

    @Modifying
    @Query("delete from IdempotenciaEntity i where i.creadoEn < :cutoff")
    int deleteExpired(Instant cutoff);
}
