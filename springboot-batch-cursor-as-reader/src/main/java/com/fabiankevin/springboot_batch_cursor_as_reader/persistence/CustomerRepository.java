package com.fabiankevin.springboot_batch_cursor_as_reader.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE customers SET status='ACTIVE' WHERE id IN(:ids)")
    void updateAllToActiveById(@Param("ids") Set<UUID> ids);
}
