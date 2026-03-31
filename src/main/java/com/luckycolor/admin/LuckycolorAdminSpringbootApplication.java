package com.luckycolor.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LuckycolorAdminSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(LuckycolorAdminSpringbootApplication.class, args);
	}

}
