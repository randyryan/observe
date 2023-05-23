/*
 * Copyright (c) 2023 Li Wan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package works.lifeops.observe.prom4j.autoconfigure;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UriBuilderFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Strings;

import works.lifeops.observe.prom4j.Prom4jProperties;
import works.lifeops.observe.prom4j.builder.PromQueryDeserializer;
import works.lifeops.observe.prom4j.builder.PromQueryResponse;
import works.lifeops.observe.prom4j.builder.PromQueryService;
import works.lifeops.observe.prom4j.builder.PromQueryUriBuilderFactory;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResultMapper;

@AutoConfiguration
@AutoConfigureAfter({ WebMvcAutoConfiguration.class })
@ConditionalOnProperty(prefix = "prom4j", name = "enabled")
@ConditionalOnWebApplication
@EnableConfigurationProperties({ Prom4jProperties.class })
public class Prom4jAutoConfiguration implements InitializingBean, WebMvcConfigurer {
  private static String PROMETHEUS_SERVER_BASE_URI;

  private final Prom4jProperties prom4jProperties;

  public Prom4jAutoConfiguration(Prom4jProperties prom4jProperties) {
    this.prom4jProperties = prom4jProperties;

    if (Strings.isNullOrEmpty(PROMETHEUS_SERVER_BASE_URI)) {
      PROMETHEUS_SERVER_BASE_URI = prom4jProperties.getPrometheus().getServer().getBaseUri();
    }
  }

  @Value("${prom4j.prometheus.server.base-uri}/api/v1")
  private void prometheusServerBaseUrl(String prometheusServerBaseUri) {
    PROMETHEUS_SERVER_BASE_URI = prometheusServerBaseUri;
  }

  @Bean("prom4jUriBuilderFactory")
  UriBuilderFactory prom4jUriBuilderFactory() {
    return UriBuilderFactoryInstanceHolder.INSTANCE;
  }

  @Bean("prom4jObjectMapper")
  ObjectMapper prom4jObjectMapper() {
    return ObjectMapperInstanceHolder.INSTANCE;
  }

  @Bean("prom4jMessageConverter")
  HttpMessageConverter<Object> prom4jMessageConverter() {
    return HttpMessageConverterInstanceHolder.INSTANCE;
  }
  
  @Bean("prom4jRestTemplate")
  RestTemplate prom4jServerRestTemplate() {
    return new RestTemplateBuilder()
        .rootUri(PROMETHEUS_SERVER_BASE_URI)
        .messageConverters(prom4jMessageConverter())
        .build();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    PROMETHEUS_SERVER_BASE_URI = prom4jProperties.getPrometheus().getServer().getBaseUri();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    // Non Spring singleton used here because the method is not "@Bean" annotated to leverage CGLIB generated proxy.
    converters.add(HttpMessageConverterInstanceHolder.INSTANCE);
  }

  @Bean("prom4jWebClient")
  WebClient prom4jWebClient() {
    // TODO: Add OAuth configuration once the Prometheus server is secured
    return WebClient.builder()
        .uriBuilderFactory(UriBuilderFactoryInstanceHolder.INSTANCE)
        .codecs(configurer -> {
          configurer.defaultCodecs().jackson2JsonEncoder(
              new Jackson2JsonEncoder(prom4jObjectMapper(), MediaType.APPLICATION_JSON));
          configurer.defaultCodecs().jackson2JsonDecoder(
              new Jackson2JsonDecoder(prom4jObjectMapper(), MediaType.APPLICATION_JSON));
        })
        .build();
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(PromQueryService.class)
  public static class PromQueryServiceConfiguration {
    @Bean
    @ConditionalOnMissingBean
    PromQueryService promQueryService(@Qualifier("prom4jWebClient") WebClient prom4jWebClient,
                                             @Qualifier("prom4jRestTemplate") RestTemplate prom4jRestTemplate) {
      return new PromQueryService(prom4jWebClient,
                                  prom4jRestTemplate,
                                  UriBuilderFactoryInstanceHolder.INSTANCE,
                                  ObjectMapperInstanceHolder.INSTANCE);
    }
  }

  @Bean
  PromQueryResultMapper promQueryResultMapper() {
    return PromQueryResultMapper.INSTANCE;
  }

  private static class UriBuilderFactoryInstanceHolder {
    private UriBuilderFactoryInstanceHolder() {}

    private static UriBuilderFactory INSTANCE = new PromQueryUriBuilderFactory(PROMETHEUS_SERVER_BASE_URI);
  }

  private static class ObjectMapperInstanceHolder {
    private ObjectMapperInstanceHolder() {}

    private static ObjectMapper createInstance() {
      SimpleModule module = new SimpleModule();
      module.addDeserializer(PromQueryResponse.class, new PromQueryDeserializer());
      // Unfortunately the JavaTimeModule, who has a serializer for OffsetDateTime, didn't work.
      module.addSerializer(new StdSerializer<OffsetDateTime>(OffsetDateTime.class) {
        private static final long serialVersionUID = 1L;

        @Override
        public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
          gen.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value));
        }
      });

      return JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .addModule(module)
          .build();
    }

    private static ObjectMapper INSTANCE = createInstance();
  }

  private static class HttpMessageConverterInstanceHolder {
    private HttpMessageConverterInstanceHolder() {}

    private static HttpMessageConverter<Object> createInstance() {
      MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
      messageConverter.setObjectMapper(ObjectMapperInstanceHolder.INSTANCE);

      return messageConverter;
    }

     private static HttpMessageConverter<Object> INSTANCE = createInstance();
  }
}
