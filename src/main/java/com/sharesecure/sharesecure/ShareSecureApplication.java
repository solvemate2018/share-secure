package com.sharesecure.sharesecure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application-${spring.profiles.active:local}.properties")
public class ShareSecureApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShareSecureApplication.class, args);
	}

}
