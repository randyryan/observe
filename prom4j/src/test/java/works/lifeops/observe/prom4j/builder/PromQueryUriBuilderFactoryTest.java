package works.lifeops.observe.prom4j.builder;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

public class PromQueryUriBuilderFactoryTest {

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

  private String prometheusServerBaseUrl = "http://prometheus:9090/api/v1/query";

  private UriBuilderFactory uriBuilderFactory;
  private UriBuilder uriBuilder;

  private PromQuery promQuery;

  @BeforeEach
  public void setUp() {
    uriBuilderFactory = new PromQueryUriBuilderFactory(prometheusServerBaseUrl);
    uriBuilder = uriBuilderFactory.builder();

    promQuery = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .label("job").equals(PromQuery.builder().value("prometheus").or("eureka"))
        .build();
  }

  @Test
  public void testUriBuilderFactory() {
    PromQueryUriBuilderFactory uriBuilderFactory = new PromQueryUriBuilderFactory(prometheusServerBaseUrl);

    Assert.assertEquals("The base URIs are equal", prometheusServerBaseUrl, uriBuilderFactory.baseUri.toUriString());
  }

  @Test
  public void testUriBuilderQueryParam() {
    URI uri = uriBuilder.queryParam("query", promQuery.toString()).build();

    String uriString = "http://prometheus:9090/api/v1/query?query=go_threads%7Bjob%3D~%22prometheus%7Ceureka%22%7D";
    Assert.assertEquals("The PromQL query URI is properly built", uriString, uri.toString());
  }

  @Test
  public void testUriBuilderQueryParams() {
    URI uri = uriBuilder.queryParams(multiValueMap(promQuery)).build();

    String uriString = "http://prometheus:9090/api/v1/query?query=go_threads%7Bjob%3D~%22prometheus%7Ceureka%22%7D";
    Assert.assertEquals("The PromQL query URI is properly built", uriString, uri.toString());
  }

}
