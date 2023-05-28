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
package works.lifeops.observe.prom4j.builder;

import static works.lifeops.observe.prom4j.builder.PromQueries.TEST_QUERY;
import static works.lifeops.observe.prom4j.builder.PromQueries.createUriFunc;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResult;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResultMapper;

@Beta
@SuppressWarnings("unused")
@Slf4j
@Service
public class PromQueryService {
  private final WebClient client;
  private final RestTemplate restTemplate;
  private final UriBuilderFactory uriBuilderFactory;
  private final ObjectMapper objectMapper;
  private final PromQueryResultMapper resultMapper;

  public PromQueryService(@Qualifier("prom4jWebClient") final WebClient client,
                          @Qualifier("prom4jRestTemplate") final RestTemplate restTemplate,
                          @Qualifier("prom4jUriBuilderFactory") final UriBuilderFactory uriBuilderFactory,
                          @Qualifier("prom4jObjectMapper") final ObjectMapper objectMapper,
                          @Qualifier("promQueryResultMapper") final PromQueryResultMapper resultMapper) {
    this.client = client;
    this.restTemplate = restTemplate;
    this.uriBuilderFactory = uriBuilderFactory;
    this.objectMapper = objectMapper;
    this.resultMapper = resultMapper;
  }

  public String getPrometheusServerBaseUri() {
    // TODO: Modify configuration to return concrete type as suggested by Spring.
    return ((PromQueryUriBuilderFactory) uriBuilderFactory).baseUri.toUriString();
  }

  /**
   * Query asynchronously using the {@link WebClient} (Spring WebFlux).
   */
  public <R extends PromQueryResponse.Result> Mono<PromQueryResponse<R>> query(PromQuery promQuery) {
    return client.get().uri(createUriFunc(promQuery)).retrieve().bodyToMono(new ParameterizedTypeReference<>() {});
  }

  /**
   * Query with blocking using the {@link RestTemplate} (Spring WebMVC).
   */
  public <R extends PromQueryResponse.Result> ResponseEntity<PromQueryResponse<R>> queryBlocking(PromQuery promQuery) {
      URI uri = PromQueries.createUri(uriBuilderFactory.builder(), promQuery);
      return restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
  }

  public void test(Optional<PromQuery> query) {
    query(query.orElse(TEST_QUERY)).subscribe(response ->
        log.info("Test query \"{}\" got response = {}", query.orElse(TEST_QUERY), response.toString()));
  }

  public void testBlocking(Optional<PromQuery> query) {
    ResponseEntity<PromQueryResponse<PromQueryResponse.VectorResult>> response =
        queryBlocking(query.orElse(TEST_QUERY));
    log.info("Test query blocking \"{}\" got response = {}", query.orElse(TEST_QUERY), response.getBody().toString());
  }

  public List<PromQueryResult.SampleResult> getSamples(PromQuery.InstantQuery instantQuery) {
    PromQueryResponse<PromQueryResponse.VectorResult> response = this
        .<PromQueryResponse.VectorResult>queryBlocking(instantQuery)
        .getBody();

    return resultMapper.vectorResponseToSampleResult(response);
  }

  public List<PromQueryResult.TimeSeriesResult> getTimeSeries(PromQuery promQuery) {
    PromQueryResponse<PromQueryResponse.MatrixResult> response = this
        .<PromQueryResponse.MatrixResult>queryBlocking(promQuery)
        .getBody();

    return resultMapper.matrixResponseToTimeSeriesResult(response);
  }

  // public abstract <R> List<PromQueryResult<R>> getResult(PromQuery<R> promQuery);
}
