package works.lifeops.observe.prom4j;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
@Configuration
@ComponentScan(basePackages = { Prom4jApiConfiguration.API_PACKAGE })
public class Prom4jApiConfiguration {
  public static final String API_PACKAGE = "works.lifeops.observe.prom4j.api";
  @Autowired
  private RequestMappingHandlerMapping requestMappingHandlerMapping;

  public Prom4jApiConfiguration() {
    // Do nothing, we only need the @ComponentScan to work here instead of using it at the Prom4jApplication
    log.info("Loading generated service stubs under package {}", Prom4jApiConfiguration.API_PACKAGE);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void listApi() {
    requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
        .map(RequestMappingInfo::toString)
        .sorted()
        .forEach(mapping -> log.info("Endpoint {}", mapping));
  }

}
