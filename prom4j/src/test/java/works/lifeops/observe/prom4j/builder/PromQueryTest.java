package works.lifeops.observe.prom4j.builder;

import static works.lifeops.observe.prom4j.builder.PromQueryBuilder.value;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class PromQueryTest {
  @Test
  public void testQueryType() {
    PromQuery query = PromQuery.builder()
        .instant()
        .build();

    Assert.assertEquals("Query type is \"instant\"", PromQuery.QueryType.INSTANT, query.type);
    Assert.assertTrue("Query is returns true on \"instant\"", query.is(PromQuery.QueryType.INSTANT));
    Assert.assertFalse("Query is returns false on \"range\"", query.is(PromQuery.QueryType.RANGE));
  }

  @Test
  public void testOnlyMetric() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .build();

    Assert.assertEquals("Only metric is equal", "go_threads", query.toString());
  }

  @Test
  public void testOnlySelector() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("job").equals("prometheus")
        .build();

    Assert.assertEquals("Only selectors is equal", "{job=\"prometheus\"}", query.toString());
  }

  @Test
  public void testBoth() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric("go_threads").time("")
        .label("job").equals("prometheus")
        .build();

    Assert.assertEquals("Both metric and selectors is equal", "go_threads{job=\"prometheus\"}", query.toString());
  }

  @Test
  public void testMultipleLabels() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("instance").equals("localhost:9090")
        .label("job").equals("prometheus")
        .build();

    Assert.assertEquals("Multiple labels is equal", "{instance=\"localhost:9090\",job=\"prometheus\"}", query.toString());
  }

  @Test
  public void testMultipleLabelValues() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("job").equals(value("prometheus").or("eureka"))
        .build();

    Assert.assertEquals("Multiple label values is equal", "{job=~\"prometheus|eureka\"}", query.toString());
  }

  @Test
  public void testDuration() {
    PromQuery query = PromQuery.builder()
            .instant()
            .metric("go_threads")
            .duration().m(1).s(30)
            .build();

    Assert.assertEquals("Duration is properly built", "go_threads[1m30s]", query.toString());
  }
}
