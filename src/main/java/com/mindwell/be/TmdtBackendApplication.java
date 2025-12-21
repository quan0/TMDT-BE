package com.mindwell.be;

import com.mindwell.be.service.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableScheduling
public class TmdtBackendApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TmdtBackendApplication.class, args);
	}

}
