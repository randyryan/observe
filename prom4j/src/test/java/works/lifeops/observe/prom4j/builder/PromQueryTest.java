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
        .metric("icluster_monitor_transactions_todotrans_gauge")
        .build();

    Assert.assertEquals("Only metric is equal", "icluster_monitor_transactions_todotrans_gauge", query.toString());
  }

  @Test
  public void testOnlySelector() {
    PromQuery query = PromQuery.builder().instant()
        .label("group").equals("GNTEST")
        .build();

    Assert.assertEquals("Only selectors is equal", "{group=\"GNTEST\"}", query.toString());
  }

  @Test
  public void testBoth() {
    PromQuery query = PromQuery.builder().instant()
        .time("")
        .metric("icluster_monitor_transactions_todotrans_gauge").time("")
        .label("group").equals(value("GNTEST"))
        .build();

    Assert.assertEquals("Both metric and selectors is equal", "icluster_monitor_transactions_todotrans_gauge{group=\"GNTEST\"}", query.toString());
  }

  @Test
  public void testMultipleLabels() {
    PromQuery query = PromQuery.builder().instant()
        .label("group").equals("GNTEST")
        .label("node").equals("ICTST73A")
        .label("job").equals("eureka")
        .build();

    Assert.assertEquals("Multiple labels is equal", "{group=\"GNTEST\",node=\"ICTST73A\",job=\"eureka\"}", query.toString());
  }

  @Test
  public void testMultipleLabelValues() {
    PromQuery query = PromQuery.builder().instant()
        .label("group").equals(value("GNTEST").or("GNSQL"))
        .build();

    Assert.assertEquals("Multiple label values is equal", "{group=~\"GNTEST|GNSQL\"}", query.toString());
  }

}
