package works.lifeops.observe.prom4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;
import works.lifeops.observe.prom4j.builder.PromQueryDeserializer;
import works.lifeops.observe.prom4j.builder.PromQueryResponse;
import works.lifeops.observe.prom4j.builder.PromQueryUriBuilderFactory;

@Configuration
@Slf4j
public class Prom4jConfiguration {

  private static String PROMETHEUS_SERVER_BASE_URL;

  @Value("${prometheus.server.base-uri}/api/v1")
  private void prometheusServerBaseUrl(String prometheusServerBaseUrl) {
    PROMETHEUS_SERVER_BASE_URL = prometheusServerBaseUrl;
  }

  @Bean("prom4jUriBuilderFactory")
  UriBuilderFactory prom4jUriBuilderFactory() {
    return UriBuilderFactoryInstanceHolder.INSTANCE;
  }

  @Bean("prom4jObjectMapper")
  ObjectMapper prome4jObjectMapper() {
    return ObjectMapperInstanceHolder.INSTANCE;
  }

  @Bean("prom4jWebClient")
  WebClient prom4jWebClient() {
    ObjectMapper objectMapper = prome4jObjectMapper();
    // TODO: Add OAuth configuration once the Prometheus server is secured
    return WebClient.builder()
        .uriBuilderFactory(prom4jUriBuilderFactory())
        .codecs(configurer -> {
          configurer.defaultCodecs().jackson2JsonEncoder(
              new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
          configurer.defaultCodecs().jackson2JsonDecoder(
              new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
        })
        .build();
  }

  @Bean("prom4jRestTemplate")
  RestTemplate prometheusServerRestTemplate() {
      MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
      messageConverter.setObjectMapper(prome4jObjectMapper());

      return new RestTemplateBuilder()
          .rootUri(PROMETHEUS_SERVER_BASE_URL)
          .messageConverters(messageConverter)
          .build();
  }

  /**
   * To avoid creating duplicate instances from invoking {@code prom4jUriBuilderFactory()} to provide dependency for
   * other beans after Spring 2.6 deemed it as circular dependency.
   */
  private static class UriBuilderFactoryInstanceHolder {
    private UriBuilderFactoryInstanceHolder() {}

    private static UriBuilderFactory INSTANCE = new PromQueryUriBuilderFactory(PROMETHEUS_SERVER_BASE_URL);
  }

  /**
   * To avoid creating duplicate instances from invoking {@code prom4jObjectMapper()} to provide dependency for other
   * beans after Spring 2.6 deemed it as circular dependency.
   */
  private static class ObjectMapperInstanceHolder {
    private ObjectMapperInstanceHolder() {}
    private static ObjectMapper createInstance() {
      SimpleModule module = new SimpleModule();
      module.addDeserializer(PromQueryResponse.class, new PromQueryDeserializer());

      return JsonMapper.builder()
          .addModule(module)
          .addModule(new JavaTimeModule())
          .build();
    }

    private static ObjectMapper INSTANCE = createInstance();
  }
}
