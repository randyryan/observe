package works.lifeops.observe.prom4j.builder;

import static works.lifeops.observe.prom4j.builder.PromQueryBuilder.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PromQueryTest {
  private static final String METRIC_NAME = "go_threads";

  @Test
  @DisplayName("PromQuery.type and PromQuery.is")
  public void instantQueryType() {
    PromQuery query = PromQuery.builder()
        .instant()
        .build();

    Assertions.assertEquals(PromQuery.QueryType.INSTANT, query.type, "PromQuery type is \"INSTANT\"");
    Assertions.assertTrue(query.is(PromQuery.QueryType.INSTANT), "PromQuery is returns true on \"INSTANT\"");
    Assertions.assertFalse(query.is(PromQuery.QueryType.RANGE), "PromQuery is returns false on \"RANGE\"");
  }

  @Test
  public void rangeQueryType() {
    PromQuery query = PromQuery.builder()
        .range()
        .build();

    Assertions.assertEquals(PromQuery.QueryType.RANGE, query.type, "PromQuery type is \"RANGE\"");
    Assertions.assertTrue(query.is(PromQuery.QueryType.RANGE), "PromQuery is returns true on \"RANGE\"");
    Assertions.assertFalse(query.is(PromQuery.QueryType.INSTANT), "PromQuery is returns false on \"INSTANT\"");
  }

  @Test
  public void metricOnly() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric(METRIC_NAME)
        .build();

    Assertions.assertEquals("go_threads", query.toString(), "PromQuery metric (only) is properly built");
  }

  @Test
  public void labelOnly() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("job").equals("prometheus")
        .build();

    Assertions.assertEquals("{job=\"prometheus\"}", query.toString(), "PromQuery label (only) is properly built");
  }

  @Test
  public void metricAndLabel() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .label("job").equals("prometheus")
        .build();

    Assertions.assertEquals("go_threads{job=\"prometheus\"}", query.toString(), "PromQuery metric and label is properly built");
  }

  @Test
  public void multipleLabels() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("instance").equals("localhost:9090")
        .label("job").equals("prometheus")
        .build();

    Assertions.assertEquals("{instance=\"localhost:9090\",job=\"prometheus\"}", query.toString(), "PromQuery multiple labels are properly built");
  }

  @Test
  public void multipleLabelValuesOr() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("job").equals(value("prometheus").or("eureka"))
        .build();

    Assertions.assertEquals("{job=~\"prometheus|eureka\"}", query.toString(), "PromQuery multiple labels or relationship is properly build");
  }

  @Test
  public void duration() {
    PromQuery query = PromQuery.builder()
            .instant()
            .metric("go_threads")
            .duration().m(1).s(30)
            .build();

    Assertions.assertEquals("go_threads[1m30s]", query.toString(), "PromQuery duration is properly built.");
  }
}
