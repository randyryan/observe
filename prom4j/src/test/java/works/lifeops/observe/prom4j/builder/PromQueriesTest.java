package works.lifeops.observe.prom4j.builder;

import java.net.URI;

import org.junit.Assert;
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

    URI uri = PromQueries.createUri(query).apply(uriBuilder);
    Assert.assertEquals("URI is properly created", "/api/v1/query?query=go_threads&time=2023-04-22T20%3A45%3A40%2B08%3A00", uri.toString());
  }
}

