package fr.yoannbarrallon.runtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RuntrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(RuntrackApplication.class, args);
	}

}
