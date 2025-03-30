package com.fabiankevin.springboot_batch_cursor_as_reader.config;

import com.fabiankevin.springboot_batch_cursor_as_reader.persistence.CustomerEntity;
import com.fabiankevin.springboot_batch_cursor_as_reader.persistence.CustomerRepository;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.stream.Collectors;

@Slf4j
@Configuration
public class CustomerBatchConfig {


    @Bean
    public JdbcCursorItemReader<CustomerEntity> customerReader(HikariDataSource hikariDataSource) {
        return new JdbcCursorItemReaderBuilder<CustomerEntity>()
                .dataSource(hikariDataSource)
                .name("customerReader")
                .sql("select ID, NAME, STATUS, CREATED_AT from CUSTOMERS WHERE STATUS=?")
                .preparedStatementSetter(ps -> ps.setString(1, "INACTIVE"))
                .maxItemCount(100_000)
                .rowMapper(new CustomerEntityRowMapper())
                .build();
    }

    @Bean
    public ItemWriter<CustomerEntity> customersUpdateWriter(CustomerRepository customerRepository) {
        return items -> {
            customerRepository.updateAllToActiveById(items.getItems().stream()
                    .map(CustomerEntity::getId).collect(Collectors.toSet()));
        };
    }

    @Bean
    public JdbcBatchItemWriter<CustomerEntity> jdbcBatchItemWriter(HikariDataSource hikariDataSource) {
        String insertSql = """
                INSERT INTO customers(id, name, status, created_at)
                VALUES(:id, :name, :status, :created_at)
                """;
        return new JdbcBatchItemWriterBuilder<CustomerEntity>()
                .dataSource(hikariDataSource)
                .sql(insertSql)
                .itemSqlParameterSourceProvider(item -> {
                    MapSqlParameterSource params = new MapSqlParameterSource();
                    params.addValue("id", item.getId());
                    params.addValue("name", item.getName());
                    params.addValue("status", item.getStatus());
                    params.addValue("created_at", item.getCreatedAt());
                    return params;
                })
                .build();
    }

    @Bean
    public Step customerStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             JdbcCursorItemReader<CustomerEntity> customerReader,
                             @Qualifier("customersUpdateWriter")
                             ItemWriter<CustomerEntity> itemWriter) {
        return new StepBuilder("customerStep", jobRepository)
                .<CustomerEntity, CustomerEntity>chunk(500, transactionManager)
                .reader(customerReader)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public Job customerEntityJob(JobRepository jobRepository, Step customerStep) {
        return new JobBuilder("customerEntityJob", jobRepository)
                .start(customerStep)
                .build();
    }
}
