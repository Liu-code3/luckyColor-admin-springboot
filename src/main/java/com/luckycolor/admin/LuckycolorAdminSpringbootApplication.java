package com.luckycolor.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.luckycolor.admin")
@ConfigurationPropertiesScan
public class LuckycolorAdminSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(LuckycolorAdminSpringbootApplication.class, args);
	}

}
