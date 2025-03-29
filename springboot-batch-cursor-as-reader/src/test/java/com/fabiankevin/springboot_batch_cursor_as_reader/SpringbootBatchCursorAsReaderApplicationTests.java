package com.fabiankevin.springboot_batch_cursor_as_reader;

import com.fabiankevin.springboot_batch_cursor_as_reader.persistence.CustomerEntity;
import com.fabiankevin.springboot_batch_cursor_as_reader.persistence.CustomerRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@SpringBatchTest
class SpringbootBatchCursorAsReaderApplicationTests {
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@BeforeEach
	public void setup(){
		List<CustomerEntity> inactive = IntStream.range(0, 10).boxed()
				.map(i -> {
					CustomerEntity customerEntity = new CustomerEntity();
					customerEntity.setCreatedAt(Instant.now());
					customerEntity.setStatus("INACTIVE");
					customerEntity.setName(UUID.randomUUID().toString());

					return customerEntity;
				}).toList();
		customerRepository.saveAll(inactive);
	}

	@Test
	void launchJob_givenInactiveCustomers_thenShouldBeUpdatedToActive() throws Exception {
		jobLauncherTestUtils.launchJob();

		long count = customerRepository.count(Example.of(CustomerEntity.builder()
				.status("ACTIVE")
				.build()));
		assertEquals(10, count, "active customers should be equal");
	}

}
