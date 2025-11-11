package com.mangawatch;

import java.time.ZoneId;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MangawatchApplication {

	public static void main(String[] args) {
		System.out.println(ZoneId.systemDefault());
		SpringApplication.run(MangawatchApplication.class, args);
	}
}
