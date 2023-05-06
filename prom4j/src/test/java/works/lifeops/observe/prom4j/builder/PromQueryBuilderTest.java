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

import static works.lifeops.observe.prom4j.builder.PromQuery.value;
import static works.lifeops.observe.prom4j.builder.PromQuery.values;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PromQueryBuilderTest {
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

  @Test
  public void range() {
    PromQuery promQuery = PromQuery.builder()
          .range()
          .metric("go_threads")
          .label("job").equals(values(List.of("prometheus", "eureka")))
          .start("2023-05-03T19:45:00+08:00")
          .end("2023-05-03T19:47:00+08:00")
          .step(10)
          .build();
  }

  @Test
  public void labelValues() {
    PromQueryBuilder.LabelValueBuilder labelValueBuilder1 = value("prometheus");
    PromQueryBuilder.LabelValueBuilder labelValueBuilder2 = value("prometheus").or("eureka");

    Assertions.assertEquals("\"prometheus\"", labelValueBuilder1.toString(), "Single value properly built");
    Assertions.assertEquals("\"prometheus|eureka\"", labelValueBuilder2.toString(), "Binary values properly built");
  }

  @Test
  public void labelValuesList() {
    PromQueryBuilder.LabelValueBuilder labelValueBuilder0 = PromQuery.values(List.of());
    PromQueryBuilder.LabelValueBuilder labelValueBuilder1 = PromQuery.values(List.of("prometheus"));
    PromQueryBuilder.LabelValueBuilder labelValueBuilder2 = PromQuery.values(List.of("prometheus", "eureka"));

    Assertions.assertEquals("\"\"", labelValueBuilder0.toString(), "Zero values properly built");
    Assertions.assertEquals("\"prometheus\"", labelValueBuilder1.toString(), "Single value properly built");
    Assertions.assertEquals("\"prometheus|eureka\"", labelValueBuilder2.toString(), "Binary values properly built");
  }
}
