package works.lifeops.observe.prom4j.config;

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
import org.springframework.web.util.UriBuilder;
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

  /**
   * For occasions when we want to obtain a request {@link java.net.URI} involves a PromQuery object, e.g. passing one
   * to RestTemplate.
   *
   * XXX: This is a wrong practice! UriBuilder instances should never get reused, always create a new instance for each
   *      request, reusing the same instance for different requests will have query parameters residency.
   *      See {@link WebClient.RequestBodyUriSpec#uri} which will always return a new instance.
   */
  @Bean("promQueryUriBuilder")
  UriBuilder promQueryUriBuilder() {
      // TODO: This creates duplicated instances, we can no longer inject the bean within the same configuration after
      //       Spring Boot 2.6. Need to come out with a solution to this.
      return promQueryUriBuilderFactory().builder();
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

  @Bean("promQueryRestTemplate")
  RestTemplate prometheusServerRestTemplate() {
      MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
      // TODO: This creates duplicated instances, we can no longer inject the bean within the same configuration after
      //       Spring Boot 2.6. Need to come out with a solution to this.
      messageConverter.setObjectMapper(prometheusObjectMapper());

      return new RestTemplateBuilder()
          .rootUri(prometheusServerBaseUrl)
          .messageConverters(messageConverter)
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
