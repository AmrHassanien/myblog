package com.example.myblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Excluding SecurityAutoConfiguration and UserDetailsServiceAutoConfiguration is required to disable security
//another exclusion is required if actuator is added in the dependency ( ManagementWebSecurityAutoConfiguration )
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class})
public class MyblogApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyblogApplication.class, args);
	}

}
