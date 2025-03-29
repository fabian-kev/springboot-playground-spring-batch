package com.fabiankevin.springboot_batch_cursor_as_reader.persistence;

import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE customers SET status='ACTIVE' WHERE id IN(:ids)")
    void updateAllToActiveById(@Param("ids") Set<UUID> ids);

    @QueryHints(value = {
            @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "2000")
    })
    Stream<CustomerEntity> findAllByStatusIn(Set<String> status);
}
