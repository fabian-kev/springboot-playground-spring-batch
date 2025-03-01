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
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class CustomerBatchConfig {
    @Bean
    public JdbcCursorItemReader<CustomerEntity> customerReader(HikariDataSource hikariDataSource) {
        return new JdbcCursorItemReaderBuilder<CustomerEntity>()
                .dataSource(hikariDataSource)
                .name("customerReader")
                .sql("select ID, NAME, STATUS, CREATED_AT from CUSTOMERS WHERE STATUS='INACTIVE'")
                .rowMapper(new CustomerEntityRowMapper())
                .build();

    }

    @Bean
    public ItemProcessor<CustomerEntity, CustomerEntity> customerEntityItemProcessor(){
        return item -> {
            item.setStatus("ACTIVE");
            return item;
        };
    }

    @Bean
    public ItemWriter<CustomerEntity> jpaCustomerWriter(CustomerRepository customerRepository){
        return chunk -> {
            customerRepository.saveAll(chunk.getItems());
            log.info("{} have been saved.", chunk.getItems().size());
        };
    }

    @Bean
    public Step customerStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             JdbcCursorItemReader<CustomerEntity> customerReader,
                             ItemWriter<CustomerEntity> jpaCustomerWriter) {
        return new StepBuilder("customerStep", jobRepository)
                .<CustomerEntity, CustomerEntity>chunk(10, transactionManager)
                .reader(customerReader)
                .processor(customerEntityItemProcessor())
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
