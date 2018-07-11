package org.ab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableAspectJAutoProxy
@ComponentScan("org.ab")
@EnableJpaRepositories
public class Week2StockApplication {

	public static void main(String[] args) {
		SpringApplication.run(Week2StockApplication.class, args);
	}
}
