package works.lifeops.observe.prom4j.builder;

import java.net.URI;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriBuilder;

/**
 * The tests in this class mainly test the aspect of appropriately appending query parameters in the URI. For handling
 * Prometheus query expressions in the "query" query parameter, see PromQueryUriBuilderFactoryTest.
 */
public class PromQueriesTest {
  private UriBuilder uriBuilder;

  @BeforeEach
  public void setUp() {
//    uriBuilder = UriComponentsBuilder.fromPath("/api/v1");
    uriBuilder = new PromQueryUriBuilderFactory("/api/v1").builder();
  }

  @Test
  public void testInstantUriCreation() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .build();

    URI uri = PromQueries.createUri(query).apply(uriBuilder);
    Assert.assertEquals("URI is properly created", "/api/v1/query?query=go_threads", uri.toString());
  }

  @Test
  public void testInstantWithTimeUriCreation() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .time("2023-04-22T20:45:40+08:00")
        .build();

    URI uri = PromQueries.createUri(query, false).apply(uriBuilder);
    System.out.println(uri.toString());
    Assert.assertEquals("URI is properly created", "/api/v1/query?query=go_threads&time=2023-04-22T20%3A45%3A40%2B08%3A00", uri.toString());
  }
}

