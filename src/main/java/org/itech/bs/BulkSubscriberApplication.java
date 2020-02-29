package org.itech.bs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableJpaRepositories("org.itech")
@EntityScan("org.itech")
@ComponentScan("org.itech")
public class BulkSubscriberApplication {

	public static void main(String[] args) {
		SpringApplication.run(BulkSubscriberApplication.class, args);
	}

}
