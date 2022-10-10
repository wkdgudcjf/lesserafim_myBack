package com.lesserafim;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.lesserafim.config.properties.AppProperties;
import com.lesserafim.config.properties.CorsProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    CorsProperties.class,
    AppProperties.class
})
public class LesserafimBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(LesserafimBackApplication.class, args);
	}
	
	@PostConstruct
	void postConstruct() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}
}
