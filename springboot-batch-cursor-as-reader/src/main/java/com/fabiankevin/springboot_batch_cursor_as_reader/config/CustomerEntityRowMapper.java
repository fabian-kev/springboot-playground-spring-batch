package com.fabiankevin.springboot_batch_cursor_as_reader.config;

import com.fabiankevin.springboot_batch_cursor_as_reader.persistence.CustomerEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CustomerEntityRowMapper implements RowMapper<CustomerEntity> {

    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String STATUS_COLUMN = "status";

    @Override
    public CustomerEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        CustomerEntity customerCredit = new CustomerEntity();

        customerCredit.setId(rs.getObject(ID_COLUMN, UUID.class));
        customerCredit.setName(rs.getString(NAME_COLUMN));
        customerCredit.setStatus(rs.getString(STATUS_COLUMN));
        customerCredit.setCreatedAt(rs.getTimestamp(CREATED_AT_COLUMN).toInstant());

        return customerCredit;
    }
}