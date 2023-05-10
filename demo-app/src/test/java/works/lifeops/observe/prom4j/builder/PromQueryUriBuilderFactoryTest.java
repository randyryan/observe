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

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

/**
 * This test tests building the proper query params: The stuff after the '?' in the URI.
 */
public class PromQueryUriBuilderFactoryTest {
  private static final String PROMETHEUS_SERVER_BASE_URL = "http://prometheus:9090/api/v1";

  private UriBuilderFactory uriBuilderFactory;
  private UriBuilder uriBuilder;

  private PromQuery instantQuery;
  private PromQuery rangeQuery;

  @BeforeEach
  public void setUp() {
    uriBuilderFactory = new PromQueryUriBuilderFactory(PROMETHEUS_SERVER_BASE_URL);
    uriBuilder = uriBuilderFactory.builder();

    instantQuery = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .label("job").equals(PromQuery.builder().value("prometheus").or("eureka"))
        .build();
    rangeQuery = PromQuery.builder()
        .range()
        .metric("go_threads")
        .start("2023-05-03T19:45:00+08:00")
        .end("2023-05-03T19:47:00+08:00")
        .step(10)
        .build();
  }

  @Test
  @DisplayName("Using correct base URI")
  public void baseUri() throws URISyntaxException {
    PromQueryUriBuilderFactory uriBuilderFactory = new PromQueryUriBuilderFactory(PROMETHEUS_SERVER_BASE_URL);

    String baseUrl = PROMETHEUS_SERVER_BASE_URL;
    String baseUriString = uriBuilderFactory.baseUri.toUriString();
    Assertions.assertEquals(baseUrl, baseUriString, "The UriBuilderFactory has correct base URI.");
  }

  @Test
  @DisplayName("UriBuilder builds URI by receiving parameters from \"queryParam\" (instant queries)")
  public void queryParamToUriInstant() throws URISyntaxException {
    URI buildUri = uriBuilder
        .path("/query")
        .queryParam("query", instantQuery.toString())
        .build();

    URI uri = new URI("http://prometheus:9090/api/v1/query?query=go_threads%7Bjob%3D~%22prometheus%7Ceureka%22%7D");
    Assertions.assertEquals(uri, buildUri, "The URI for an instant query is properly built");
  }

  @Test
  @DisplayName("UriBuilder builds URI by receiving parameters from \"queryParam\" (range queries)")
  public void queryParamToUriRange() throws URISyntaxException {
    URI buildUri = uriBuilder
        .path("/query_range")
        .queryParam("query", rangeQuery.toString())
        .build();

    URI uri = new URI("http://prometheus:9090/api/v1/query_range?query=go_threads");
    Assertions.assertEquals(uri, buildUri, "The URI for an range query is properly built");
  }

  /**
   * This is not a "pure" unit test as it involves the correctness of {@link PromQueries#toQueryParams}.
   *
   * This test tests putting the query parameters other than the "query" parameter.
   */
  @Test
  @DisplayName("UriBuilder builds URI by receiving parameters from \"queryParams\" (instant queries)")
  public void queryParamsToUriInstant() throws URISyntaxException {
    URI buildUri = uriBuilder
        .path("/query")
        .queryParams(PromQueries.toQueryParams(instantQuery))
        .build();

    URI uri = new URI("http://prometheus:9090/api/v1/query?query=go_threads%7Bjob%3D~%22prometheus%7Ceureka%22%7D");
    Assertions.assertEquals(uri, buildUri, "The URI for an instant query is properly built");
  }

  /**
   * This is not a "pure" unit test as it involves the correctness of {@link PromQueries#toQueryParams}.
   *
   * This test tests putting the query parameters other than the "query" parameter.
   */
  @Test
  @DisplayName("UriBuilder builds URI by receiving parameters from \"queryParams\" (range queries)")
  public void queryParamsToUriRange() throws URISyntaxException {
    URI buildUri = uriBuilder
        .path("/query_range")
        .queryParams(PromQueries.toQueryParams(rangeQuery))
        .build();

    URI uri = new URI("http://prometheus:9090/api/v1/query_range?query=go_threads&start=2023-05-03T19%3A45%3A00%2B08%3A00&end=2023-05-03T19%3A47%3A00%2B08%3A00&step=10");
    Assertions.assertEquals(uri, buildUri, "The URI for an range query is properly built");
  }
}
