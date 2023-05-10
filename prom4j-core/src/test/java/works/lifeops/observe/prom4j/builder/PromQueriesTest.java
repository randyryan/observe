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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

/**
 * The tests in this class mainly test the aspect of appropriately appending query parameters in the URI. For handling
 * Prometheus query expressions in the "query" query parameter specifically, see PromQueryUriBuilderFactoryTest.
 */
public class PromQueriesTest {
  private static UriBuilderFactory URI_BUILDER_FACTORY;
  private UriBuilder uriBuilder;

  @BeforeAll
  public static void setUpClass() {
    URI_BUILDER_FACTORY = new PromQueryUriBuilderFactory("/api/v1");
  }

  @BeforeEach
  public void setUp() {
    uriBuilder = URI_BUILDER_FACTORY.builder();
  }

  @Test
  public void testInstantUriCreation() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .build();

    URI uri = PromQueries.createUriFunc(query).apply(uriBuilder);
    Assertions.assertEquals("/api/v1/query?query=go_threads", uri.toString(), "URI is properly created");
  }

  @Test
  public void testInstantWithTimeUriCreation() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .time("2023-04-22T20:45:40+08:00")
        .build();

    URI uri = PromQueries.createUriFunc(query).apply(uriBuilder);
    Assertions.assertEquals("/api/v1/query?query=go_threads&time=2023-04-22T20%3A45%3A40%2B08%3A00", uri.toString(), "URI is properly created");
  }
}

