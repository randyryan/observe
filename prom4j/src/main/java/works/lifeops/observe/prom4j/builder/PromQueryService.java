package works.lifeops.observe.prom4j.builder;

import static works.lifeops.observe.prom4j.builder.PromQueries.TEST_QUERY;
import static works.lifeops.observe.prom4j.builder.PromQueries.createUri;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Beta
@SuppressWarnings("unused")
@Slf4j
@Service
public class PromQueryService {
  private final WebClient client;
  private final RestTemplate restTemplate;
  private final UriBuilder uriBuilder;
  private final ObjectMapper objectMapper;

  private PromQueryService(@Qualifier("promQueryWebClient") final WebClient client,
                           @Qualifier("promQueryRestTemplate") final RestTemplate restTemplate,
                           @Qualifier("promQueryUriBuilder") final UriBuilder uriBuilder,
                           @Qualifier("promObjectMapper") final ObjectMapper objectMapper) {
    this.client = client;
    this.restTemplate = restTemplate;
    this.uriBuilder = uriBuilder;
    this.objectMapper = objectMapper;
  }

  /**
   * Query asynchronously using the {@link WebClient} (Spring WebFlux).
   */
  public <R extends PromQueryResponse.Result> Mono<PromQueryResponse<R>> query(PromQuery promQuery) {
    return client.get().uri(createUri(promQuery)).retrieve().bodyToMono(new ParameterizedTypeReference<>() {});
  }

  public void test(Optional<PromQuery> query) {
    query(query.orElse(TEST_QUERY)).subscribe(response ->
        log.info("Test query \"{}\" got response = {}", query.orElse(TEST_QUERY), response.toString()));
  }

  /**
   * Query with blocking using the {@link RestTemplate} (Spring WebMVC).
   */
  public <R extends PromQueryResponse.Result> ResponseEntity<PromQueryResponse<R>> queryBlocking(PromQuery promQuery) {
      URI uri = createUri(promQuery).apply(uriBuilder);
      return restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
  }

  public void testBlocking(Optional<PromQuery> query) {
    ResponseEntity<PromQueryResponse<PromQueryResponse.VectorResult>> response =
        queryBlocking(query.orElse(TEST_QUERY));
    log.info("Test query blocking \"{}\" got response = {}", query.orElse(TEST_QUERY), response.getBody().toString());
  }

}
