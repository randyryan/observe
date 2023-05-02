package works.lifeops.observe.prom4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import(Prom4jApiConfiguration.class)
public class Prom4jApplication {
	public static void main(String[] args) {
		SpringApplication.run(Prom4jApplication.class, args);
	}
}
