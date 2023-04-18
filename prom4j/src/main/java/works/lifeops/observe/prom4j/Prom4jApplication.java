package works.lifeops.observe.prom4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Prom4jApplication {

	public static void main(String[] args) {
		SpringApplication.run(Prom4jApplication.class, args);
	}

}
