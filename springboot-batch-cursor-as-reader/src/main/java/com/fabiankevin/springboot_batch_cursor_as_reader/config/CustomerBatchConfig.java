package com.fabiankevin.springboot_batch_cursor_as_reader.config;

import com.fabiankevin.springboot_batch_cursor_as_reader.persistence.CustomerEntity;
import com.fabiankevin.springboot_batch_cursor_as_reader.persistence.CustomerRepository;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public ItemProcessor<CustomerEntity, CustomerEntity> customerEntityItemProcessor() {
        return item -> {
            item.setStatus("ACTIVE");
            return item;
        };
    }

    @Bean
    public ItemWriter<CustomerEntity> jpaCustomerWriter(CustomerRepository customerRepository) {
        return chunk -> {
            customerRepository.updateAllToActiveById(chunk.getItems().stream()
                    .map(CustomerEntity::getId)
                    .collect(Collectors.toSet()));
            log.info("{} have been saved.", chunk.getItems().size());
        };
    }

    //StatelessSession Limitation:
    //StatelessSession doesn’t use the persistence context, so features like dirty checking or cascading don’t apply. This is fine for your use case (simple updates), but it’s worth noting.
    @Bean("statelessItemWriter")
    public ItemWriter<CustomerEntity> statelessItemWriter(EntityManagerFactory entityManagerFactory) {
        return items -> {
            StatelessSession session = entityManagerFactory.unwrap(SessionFactory.class).openStatelessSession();
            Transaction tx = session.beginTransaction();
            try {
                for (CustomerEntity item : items) {
                    session.update(item);
                }
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            } finally {
                session.close();
            }
        };
    }

    @Bean
    public Step customerStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             JdbcCursorItemReader<CustomerEntity> customerReader,
                             ItemProcessor<CustomerEntity, CustomerEntity> customerEntityItemProcessor,
                             ItemWriter<CustomerEntity> jpaCustomerWriter) {
        return new StepBuilder("customerStep", jobRepository)
                .<CustomerEntity, CustomerEntity>chunk(500, transactionManager)
                .reader(customerReader)
                .processor(customerEntityItemProcessor)
                .writer(jpaCustomerWriter)
                .build();
    }

    @Bean
    public Job customerEntityJob(JobRepository jobRepository, Step customerStep) {
        return new JobBuilder("customerEntityJob", jobRepository)
                .start(customerStep)
                .build();
    }
}
