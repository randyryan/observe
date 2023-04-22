package works.lifeops.observe.prom4j.builder;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
  private static final PromQuery TEST_QUERY = PromQuery.builder()
      .instant()
      .metric("1")
      .build();

  /**
   * Builds the MultiValueMap required by the UriComponentsBuilder.queryParams for building
   * the query parameters into URI, by the specified Map that contains the query parameters.
   */
  private static <K, V> MultiValueMap<K, V> multiValueMap(Map<K, V> map) {
    MultiValueMap<K, V> multiValueMap = new LinkedMultiValueMap<K, V>();
    map.keySet().forEach(key -> multiValueMap.put(key, Arrays.asList(map.get(key))));

    return multiValueMap;
  }

  private static MultiValueMap<String, String> multiValueMap(PromQuery promQuery) {
    return multiValueMap(Map.of("query", promQuery.toString()));
  }

  private static Function<UriBuilder, URI> addToQueryParam(PromQuery promQuery) {
    return uriBuilder -> uriBuilder.queryParam("query", promQuery.toString()).build();
  }

  private static Function<UriBuilder, URI> queryUri(PromQuery promQuery) {
    final String path = promQuery.is(PromQuery.QueryType.INSTANT) ?
        "/query" :
        promQuery.is(PromQuery.QueryType.RANGE) ?
            "/query_range" :
            "";
    final String type = promQuery.is(PromQuery.QueryType.INSTANT) ?
        "instant" :
        promQuery.is(PromQuery.QueryType.RANGE) ?
            "range" :
            "unknown";
    log.info("PromQL \"{}\" ({}) -> {}", promQuery.toString(), type, "/api/v1" + path);

    MultiValueMap<String, String> queryParams = multiValueMap(promQuery);

    return uriBuilder -> {
      if (promQuery.is(PromQuery.QueryType.INSTANT)) {
        promQuery.asInstant().time().ifPresent(time -> uriBuilder.queryParam("time", time));
      }
      if (promQuery.is(PromQuery.QueryType.RANGE)) {
        promQuery.asRange().start().ifPresent(start -> uriBuilder.queryParam("start", start));
        promQuery.asRange().end().ifPresent(end -> uriBuilder.queryParam("end", end));
      }
      return uriBuilder
          .path(path)
          .queryParams(multiValueMap(promQuery))
          .build();
    };
  }

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
    return client.get().uri(queryUri(promQuery)).retrieve().bodyToMono(new ParameterizedTypeReference<>() {});
  }

  public void test(Optional<PromQuery> query) {
    query(query.orElse(TEST_QUERY)).subscribe(response ->
        log.info("Test query \"{}\" got response = {}", query.orElse(TEST_QUERY), response.toString()));
  }

  /**
   * Query with blocking using the {@link RestTemplate} (Spring WebMVC).
   */
  public <R extends PromQueryResponse.Result> ResponseEntity<PromQueryResponse<R>> queryBlocking(PromQuery promQuery) {
      URI uri = queryUri(promQuery).apply(uriBuilder);
      return restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
  }

  public void testBlocking(Optional<PromQuery> query) {
    ResponseEntity<PromQueryResponse<PromQueryResponse.VectorResult>> response =
        queryBlocking(query.orElse(TEST_QUERY));
    log.info("Test query blocking \"{}\" got response = {}", query.orElse(TEST_QUERY), response.getBody().toString());
  }

}
