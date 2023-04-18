package works.lifeops.observe.prom4j.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import works.lifeops.observe.prom4j.builder.PromQueryDeserializer;
import works.lifeops.observe.prom4j.builder.PromQueryResponse;
import works.lifeops.observe.prom4j.builder.PromQueryUriBuilderFactory;

@Configuration
public class PrometheusConfiguration {

  @Value("${prometheus.server.base-uri}/api/v1")
  private String prometheusServerBaseUrl;

  @Bean("promQueryUriBuilderFactory")
  UriBuilderFactory promQueryUriBuilderFactory() {
    return new PromQueryUriBuilderFactory(prometheusServerBaseUrl);
  }

  @Bean("promQueryWebClient")
  WebClient prometheusServerWebClient() {
    // TODO: This creates duplicated instances, we can no longer inject the bean within the same configuration after
    //       Spring Boot 2.6. Need to come out with a solution to this.
    ObjectMapper objectMapper = prometheusObjectMapper();
    // TODO: Add OAuth configuration once the Prometheus server is secured
    return WebClient.builder()
        .uriBuilderFactory(promQueryUriBuilderFactory())
        .codecs(configurer -> {
          configurer.defaultCodecs().jackson2JsonEncoder(
              new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
          configurer.defaultCodecs().jackson2JsonDecoder(
              new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
        })
        .build();
  }

  @Bean("promObjectMapper")
  ObjectMapper prometheusObjectMapper() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(PromQueryResponse.class, new PromQueryDeserializer());

    return JsonMapper.builder()
        .addModule(module)
        .build();
  }

}
