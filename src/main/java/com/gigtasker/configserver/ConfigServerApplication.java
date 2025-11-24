package com.gigtasker.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    private ConfigServerApplication() {}

	static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
