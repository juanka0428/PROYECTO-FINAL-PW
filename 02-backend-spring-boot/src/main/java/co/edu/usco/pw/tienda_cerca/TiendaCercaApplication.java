package co.edu.usco.pw.tienda_cerca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TiendaCercaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TiendaCercaApplication.class, args);
	}

}
