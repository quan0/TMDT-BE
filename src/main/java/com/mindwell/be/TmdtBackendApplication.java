package com.mindwell.be;

import com.mindwell.be.service.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TmdtBackendApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TmdtBackendApplication.class, args);
	}

}
