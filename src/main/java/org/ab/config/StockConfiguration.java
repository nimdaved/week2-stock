package org.ab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonFactory;

@Configuration
public class StockConfiguration {
	
	@Bean
	JsonFactory jsonfactory() {
		return new JsonFactory();
	}
	

}
