package com.example.Altaska;

import com.example.Altaska.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AltaskaApplication {

	public static void main(String[] args) {
		EnvLoader.load();
		SpringApplication.run(AltaskaApplication.class, args);
	}

}
