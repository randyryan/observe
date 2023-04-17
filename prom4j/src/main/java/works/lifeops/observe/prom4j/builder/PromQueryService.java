package works.lifeops.observe.prom4j.builder;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;

import reactor.core.publisher.Mono;

@Beta
@SuppressWarnings("unused")
@Service
public class PromQueryService {

  private static final Logger logger = LoggerFactory.getLogger(PromQueryService.class);

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
    logger.info("PromQL \"{}\" -> {}", promQuery.toString(), "/api/v1" + path);

    MultiValueMap<String, String> queryParams = multiValueMap(promQuery);

    return uriBuilder -> {
      if (promQuery.is(PromQuery.QueryType.INSTANT)) {
        logger.info("PromQuery is instant");
        promQuery.asInstant().time().ifPresent(time -> uriBuilder.queryParam("time", time));
      }
      if (promQuery.is(PromQuery.QueryType.RANGE)) {
        logger.info("PromQuery is range");
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
  private final ObjectMapper objectMapper;

  private PromQueryService(@Qualifier("promQueryWebClient") final WebClient client,
                           @Qualifier("promObjectMapper") final ObjectMapper objectMapper) {
    this.client = client;
    this.objectMapper = objectMapper;
  }

  public void test(String query) {
    PromQuery promQuery = PromQuery.builder()
        .instant()
        .metric(query)
        .build();

    client.get().uri(queryUri(promQuery)).retrieve().bodyToMono(Map.class).subscribe(map -> {
      logger.info("PromQL \"{}\" map = {}", query, map.toString());
    });
    client.get().uri(queryUri(promQuery)).retrieve().bodyToMono(Map.class).subscribe(map -> {
      String json;
      try {
        json = objectMapper.writeValueAsString(map);
      } catch (JsonProcessingException e) {
        json = "";
      }
      logger.info("PromQL \"{}\" json = {}", query, json);
    });
    client.get().uri(queryUri(promQuery)).retrieve().bodyToMono(PromQueryResponse.class).subscribe(response -> {
      logger.info("PromQL \"{}\" promQueryResponse = {}", query, response.toString());
    });
  }

  public Mono<PromQueryResponse> query(PromQuery promQuery) {
    return client.get().uri(queryUri(promQuery)).retrieve().bodyToMono(PromQueryResponse.class);
  }

}
